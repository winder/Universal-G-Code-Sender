#!/usr/bin/env python3

import requests
import os
import os.path
import json
from pprint import pprint

API_ENDPOINT = 'https://poeditor.com/api/'
PROJECT_ID = '52743'
""" relative path and root of MessagesBundle property files """
RESOURCE_ROOT = '../ugs-core/src/resources/MessagesBundle_'
REFERENCE_LANGUAGE = 'en'

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
        'de': 'de_DE',
        'el': 'el_EL',
        'en': 'en_US',
        'es': 'es_ES',
        'fr': 'fr_FR',
        'it': 'it_IT',
        'ja': 'jp_JA',
        'lt': 'lt_LT',
        'nb': 'nb_NO',
        'nl': 'nl_NL',
        'pl': 'pl_PL',
        'pt-br': 'pt_BR',
        'ru': 'ru_RU',
        'sv': 'sv_SE',
        'tr': 'tr_TR',
        'zh-CN': 'zh_CN'
        }


def getProjectData():
    """ Common data across all POEditor methods. """
    return {
            'id': PROJECT_ID,
            'api_token': API_KEY
            }


def listLanguages():
    """ Get list of languages from POEditor API. """
    data = getProjectData()
    data['action'] = 'list_languages'
    return requests.post(API_ENDPOINT, data=data)


def getLanguageFileURL(language):
    """ Get POEditor property file url for a given language. """
    data = getProjectData()
    data['action'] = 'export'
    data['type'] = 'properties'
    data['language'] = language
    r = requests.post(API_ENDPOINT, data=data)
    return r.json()['item']


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
    for rec in langs['list']:
        if rec['code'] not in LANGUAGES:
            missing.append('%s(%s)' % (rec['name'], rec['code']))

    if len(missing) is not 0:
        print('\n%d missing file mappings: %s' %
              (len(missing), ', '.join(missing)))
        os._exit(1)


if __name__ == '__main__':
    checkForMissingMapping()

    for key, value in LANGUAGES.items():
        if key is REFERENCE_LANGUAGE:
            print('Skipping reference language "%s".' % key)
            continue
        try:
            filename = RESOURCE_ROOT + value + '.properties'
            print('Updating %s: %s' % (key, filename))
            url = getLanguageFileURL(key)
            print('   url: %s' % url)
            downloadUrlToFile(url, filename)
        except Exception as e:
            print('\nProblem processing "%s": %s' % (key, str(e)))
            os._exit(1)
        print('   done.')
