#!/usr/bin/env python3

import argparse
import json
import os
import os.path
import pprint
import requests
import sys

API_ENDPOINT = 'https://api.poeditor.com/v2'
PROJECT_ID = '52743'
""" relative path and root of MessagesBundle property files """
DEFAULT_RESOURCE_ROOT = '../ugs-core/src/resources/MessagesBundle_'
REFERENCE_LANGUAGE = 'en'
pp = pprint.PrettyPrinter(indent=4)

parser = argparse.ArgumentParser(description='Change prefix of files matching a pattern')
parser.add_argument('--upload', action='store_true', help='Upload new terms to POEditor using the reference language (%s).' % REFERENCE_LANGUAGE)
parser.add_argument('--download', action='store_true', help='Download terms from POEditor and update message bundle files."')
parser.add_argument('root', default=DEFAULT_RESOURCE_ROOT, nargs='?', help='Path to message bundle file root.')

try:
    API_KEY = os.environ['POEDITOR_API_KEY']
except KeyError as e:
    print('\nPlease set the environment variable %s' % e.args[0])
    os._exit(1)

"""
This is a mapping between the POEditor language identifier (key) and
the UGS MessagesBundle_(value).properties file. Add the mapping for
any new languages to include it in the update script.
"""
LANGUAGES = {
        'af': 'af_ZA',
        'ca': 'ca_AD',
        'cs': 'cs_CZ',
        'de': 'de_DE',
        'el': 'el_EL',
        'en': 'en_US',
        'es': 'es_ES',
        'fa': 'fa_IR',
        'fr': 'fr_FR',
        'it': 'it_IT',
        'ja': 'jp_JA',
        'lt': 'lt_LT',
        'nb': 'nb_NO',
        'nn': 'nn_NO',
        'nl': 'nl_NL',
        'pl': 'pl_PL',
        'pt-br': 'pt_BR',
        'ru': 'ru_RU',
        'sk': 'sk_SK',
        'sv': 'sv_SE',
        'tr': 'tr_TR',
        'uk': 'uk_UA',
        'zh-CN': 'zh_CN',
        'zh-Hans': 'zh_Hans'
        }


def getProjectData():
    """ Common data across all POEditor methods. """
    return {
            'id': PROJECT_ID,
            'api_token': API_KEY
            }


def validateResponse(response):
    if (response.status_code != requests.codes.ok):
        pp.pprint(response)
        pp.pprint(response.json())
        raise("Bad response.")


def listLanguages():
    """ Get list of languages from POEditor API. """
    data = getProjectData()
    response = requests.post("%s/languages/list" % API_ENDPOINT, data=data)
    validateResponse(response)
    return response


def getLanguageFileURL(language):
    """ Get POEditor property file url for a given language. """
    data = getProjectData()
    data['type'] = 'properties'
    data['language'] = language
    response = requests.post("%s/projects/export" % API_ENDPOINT, data=data)
    validateResponse(response)
    return response.json()['result']['url']


def downloadUrlToFile(url, target_file):
    """ Download data at a given url and save it to a given file. """
    r = requests.get(url)

    if not os.path.isfile(target_file):
        raise Exception('Target file "%s" does not exist.' % target_file)

    os.remove(target_file)

    with open(target_file, 'wb') as f:
        for chunk in r.iter_content(chunk_size=1024):
            if chunk:  # filter out keep-alive new chunks
                f.write(chunk)


def checkForMissingMapping():
    """ Make sure all languages in POEditor are mapped to a property file. """
    langs = listLanguages().json()

    missing = []
    for rec in langs['result']['languages']:
        if rec['code'] not in LANGUAGES:
            missing.append('%s(%s)' % (rec['name'], rec['code']))

    if len(missing) != 0:
        print('\n%d missing file mappings: %s' %
              (len(missing), ', '.join(missing)))
        os._exit(1)


def updateTerms(root):
    filepath = '%s%s.properties' % (root, LANGUAGES[REFERENCE_LANGUAGE])
    print('Synchronizing terms file %s' % filepath)

    files = {
        'api_token': (None, API_KEY),
        'id': (None, PROJECT_ID),
        'updating': (None, 'terms_translations'),
        'file': (filepath, open(filepath, 'rb')),
        'language': (None, REFERENCE_LANGUAGE),
        'overwrite': (None, '1'),
        'sync_terms': (None, '1'),
        'fuzzy_trigger': (None, '1')
        }
    response = requests.post('https://api.poeditor.com/v2/projects/upload', files=files)
    validateResponse(response)


if __name__ == '__main__':
    args = parser.parse_args()

    checkForMissingMapping()

    if not args.upload and not args.download:
        print("Usage: run with 'upload' and/or 'download' argument to upload terms and/or download translations.")
        sys.exit(0)

    # Upload new terms.
    if args.upload:
        updateTerms(args.root)

    # Download new translations.
    if args.download:
        for key, value in LANGUAGES.items():
            if key is REFERENCE_LANGUAGE:
                print('Skipping reference language "%s".' % key)
                continue
            try:
                filename = args.root + value + '.properties'
                print('Updating %s: %s' % (key, filename))
                url = getLanguageFileURL(key)
                print('   url: %s' % url)
                downloadUrlToFile(url, filename)
            except Exception as e:
                print('\nProblem processing "%s": %s' % (key, str(e)))
                os._exit(1)
            print('   done.')
