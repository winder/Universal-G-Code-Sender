/*
   Copyright 2005 Simon Mieth

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.kabeja.processing;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.kabeja.dxf.DXFDocument;
import org.kabeja.processing.helper.MergeMap;
import org.kabeja.tools.SAXFilterConfig;
import org.kabeja.xml.SAXFilter;
import org.kabeja.xml.SAXGenerator;
import org.kabeja.xml.SAXSerializer;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 * 
 */
public class ProcessPipeline {
	private ProcessingManager manager;
	private List postProcessorConfigs = new ArrayList();
	private List saxFilterConfigs = new ArrayList();
	private SAXGenerator generator;
	private Map serializerProperties = new HashMap();
	private Map generatorProperties = new HashMap();
	private SAXSerializer serializer;
	private String name;
	private String description = "";

	public void process(DXFDocument doc, Map context, OutputStream out)
			throws ProcessorException {
		ContentHandler handler = null;

		// postprocess
		Iterator i = this.postProcessorConfigs.iterator();

		while (i.hasNext()) {
			PostProcessorConfig ppc = (PostProcessorConfig) i.next();
			PostProcessor pp = this.manager.getPostProcessor(ppc
					.getPostProcessorName());

			// backup the default props
			Map oldProps = pp.getProperties();
			// setup the pipepine props
			pp.setProperties(new MergeMap(ppc.getProperties(), context));
			pp.process(doc, context);
			// restore the default props
			pp.setProperties(oldProps);
		}

		List saxFilterProperties = new ArrayList();

		// setup saxfilters
		if (this.saxFilterConfigs.size() > 0) {
			i = saxFilterConfigs.iterator();
			SAXFilterConfig sc = (SAXFilterConfig) i.next();
			SAXFilter first = this.manager.getSAXFilter(sc.getFilterName());
			saxFilterProperties
					.add(new MergeMap(first.getProperties(), context));

			first.setContentHandler(this.serializer);
			handler = first;
			first.setProperties(sc.getProperties());
           
			while (i.hasNext()) {
				sc = (SAXFilterConfig) i.next();
				SAXFilter f = this.manager.getSAXFilter(sc.getFilterName());
				f.setContentHandler(first);
				saxFilterProperties.add(f.getProperties());
				f.setProperties(sc.getProperties());
				first = f;
			
			}

		} else {
			// no filter
			handler = this.serializer;
		}

		Map oldProbs = this.serializer.getProperties();
		this.serializer.setProperties(new MergeMap(this.serializerProperties,
				context));

		// invoke the filter and serializer
		this.serializer.setOutput(out);

		try {
			Map oldGenProps = this.generator.getProperties();
			this.generator.setProperties(this.generatorProperties);
			this.generator.generate(doc, handler, context);
			// restore the old props
			this.generator.setProperties(oldGenProps);
		} catch (SAXException e) {
			throw new ProcessorException(e);
		}

		// restore the serializer properties
		this.serializer.setProperties(oldProbs);

		// restore the filter properties
		for (int x = 0; x < saxFilterProperties.size(); x++) {
			SAXFilterConfig sc = (SAXFilterConfig) saxFilterConfigs.get(x);
			this.manager.getSAXFilter(sc.getFilterName()).setProperties(
					(Map) saxFilterProperties.get(x));
		}
	}

	/**
	 * @return Returns the serializer.
	 */
	public SAXSerializer getSAXSerializer() {
		return serializer;
	}

	/**
	 * @param serializer
	 *            The serializer to set.
	 */
	public void setSAXSerializer(SAXSerializer serializer) {
		this.serializer = serializer;
	}

	/**
	 * @return Returns the manager.
	 */
	public ProcessingManager getProcessorManager() {
		return manager;
	}

	/**
	 * @param manager
	 *            The manager to set.
	 */
	public void setProcessorManager(ProcessingManager manager) {
		this.manager = manager;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	public void prepare() {
	}

	public List getPostProcessorConfigs() {
		return this.postProcessorConfigs;
	}

	public void addSAXFilterConfig(SAXFilterConfig config) {
		this.saxFilterConfigs.add(config);
	}

	public void addPostProcessorConfig(PostProcessorConfig config) {
		this.postProcessorConfigs.add(config);
	}

	/**
	 * @return Returns the serializerProperties.
	 */
	public Map getSerializerProperties() {
		return serializerProperties;
	}

	/**
	 * @param serializerProperties
	 *            The serializerProperties to set.
	 */
	public void setSAXSerializerProperties(Map serializerProperties) {
		this.serializerProperties = serializerProperties;
	}

	public void setSAXGeneratorProperties(Map generatorProperties) {
		this.generatorProperties = generatorProperties;
	}

	public Map getSAXGeneratorProperties(Map generatorProperties) {
		return this.generatorProperties;
	}

	public void setSAXGenerator(SAXGenerator generator) {
		this.generator = generator;
	}

	public SAXGenerator getSAXGenerator() {
		return this.generator;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
