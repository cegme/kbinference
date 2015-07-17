
package com.cegme.kbinference.graph

import com.google.common.io.Files
import com.google.common.base.Charsets

import groovy.util.logging.Slf4j

import java.io.File


@Slf4j
class SankeyBuilder {

  static Properties props = new Properties();

  
  static String buildPage(String jsonString, ArrayList<Path> paths) {

    props.load(SankeyBuilder.class.getResourceAsStream('/app.properties'));
    def templateReader = new BufferedReader(new InputStreamReader(SankeyBuilder.class.getResourceAsStream('/templates/sankey.html'), Charsets.UTF_8))
    StringBuilder sb = new StringBuilder();
    String line = null;
    while ((line = templateReader.readLine()) != null) {
      sb.append(line);
      sb.append("\n")
    }
    def templateText = sb.toString()

    
    def sankeyReader = new BufferedReader(new InputStreamReader(SankeyBuilder.class.getResourceAsStream('/templates/sankey.js'), Charsets.UTF_8))
    sb.setLength(0)
    while ((line = sankeyReader.readLine()) != null) {
      sb.append(line);
      sb.append("\n")
    }
    def sankeyString = sb.toString()

    def dynatablejsReader = new BufferedReader(new InputStreamReader(SankeyBuilder.class.getResourceAsStream('/templates/jquery.dynatable.js'), Charsets.UTF_8))
    sb.setLength(0)
    while ((line = dynatablejsReader.readLine()) != null) {
      sb.append(line);
      sb.append("\n")
    }
    def dynatablejsString = sb.toString()

    def dynatablecssReader = new BufferedReader(new InputStreamReader(SankeyBuilder.class.getResourceAsStream('/templates/jquery.dynatable.css'), Charsets.UTF_8))
    sb.setLength(0)
    while ((line = dynatablecssReader.readLine()) != null) {
      sb.append(line);
      sb.append("\n")
    }
    def dynatablecssString = sb.toString()



    def template = new groovy.text.StreamingTemplateEngine().createTemplate(templateText);
    def binding = [
      myjsondata : jsonString,
      sankeyjs : sankeyString,
      dynatablecss : dynatablecssString,
      dynatablejs : dynatablejsString,
      pathjson : Path.toJson(paths)
    ]

    String response = template.make(binding);
    //log.info("Response page: " + response);

    return response;
  }

  /**
    * Write the sankey page to the given location.
    */
  static void writePage(String contents, String location) {
    props.load(SankeyBuilder.class.getResourceAsStream('/app.properties'));
    //def templateStringFile = new File(props.getProperty('sankeyout.file'));

    try {
    def file = new File(location)
    file.getParentFile().mkdir()
    file.createNewFile()
    def writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));

    writer.write(contents);

    writer.close();
    }
    catch (IOException ioe) {
      log.error("Could not write the file ", ioe);
    }

  }


}

