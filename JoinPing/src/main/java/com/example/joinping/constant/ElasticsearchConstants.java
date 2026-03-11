package com.example.joinping.constant;

/**
 * ES相关常量类
 */
public class ElasticsearchConstants {
    /**
     * 话题索引库名称
     */
    public static final String TOPIC_INDEX = "topic";
    /**
     * 分词器名称
     */
    public static final String ANALYZER_NAME = "ik_smart";
    /**
     * 话题索引库创建模板
     */
    public static final String TOPIC_MAPPING_TEMPLATE = "{\n" +
            "  \"mappings\": {\n" +
            "    \"properties\": {\n" +
            "      \"id\": {\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"name\": {\n" +
            "        \"type\": \"text\",\n" +
            "        \"analyzer\": \"ik_max_word\",\n" +
            "        \"fields\": {\n" +
            "          \"keyword\": {\n" +
            "            \"type\": \"keyword\"\n" +
            "          }\n" +
            "        }\n" +
            "      },\n" +
            "      \"relaNames\": {\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"createTime\": {\n" +
            "        \"type\": \"date\",\n" +
            "        \"format\": \"epoch_millis\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
    
    
}


/*

==================ES创建话题索引库的模板==================
PUT /topic
{
  "mappings": {
    "properties": {
      "id": {
        "type": "keyword"
      },
      "name": {
        "type": "text",
        "analyzer": "ik_max_word",
        "fields": {
          "keyword": {
            "type": "keyword"
          }
        }
      },
      "relaNames": {
        "type": "keyword"
      },
      "createTime": {
        "type": "date",
        "format": "epoch_millis"
      }
    }
  }
}

**/