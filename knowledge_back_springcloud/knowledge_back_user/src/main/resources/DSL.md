```
PUT /article
{
  "settings": {
    "analysis": {
      "analyzer": {
        "text_anlyzer": {   
          "tokenizer": "ik_max_word",
          "filter": "py"
        },
        "completion_analyzer": {     
          "tokenizer": "keyword",   
          "filter": "py"
        }
      },
      "filter": {
        "py": {
          "type": "pinyin",
          "keep_full_pinyin": false,
          "keep_joined_full_pinyin": true,
          "keep_original": true,
          "limit_first_letter_length": 16,
          "remove_duplicated_term": true,
          "none_chinese_pinyin_tokenize": false
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "articleId":{
        "type": "keyword"
      },
      "title":{
        "type": "text",
        "analyzer": "text_anlyzer",
        "search_analyzer": "ik_smart",
        "copy_to": "all"
      },
      "userId":{
        "type": "keyword"
      },
      "likeCount":{
        "type": "integer"
      },
      "commentCount":{
        "type": "integer",
        "index": false
      },
      "collectCount":{
        "type": "integer",
        "index": false
      },
      "scanCount":{
        "type": "integer"
      },
      "articleClass":{
        "type": "keyword",
        "copy_to": "all"
      },
      "articleContent":{
        "type": "text",
        "analyzer": "text_anlyzer",
        "search_analyzer": "ik_smart",
        "copy_to": "all"
      },
      "articleTime":{
        "type": "date"
      },
      "articleState":{
        "type": "keyword"
      },
      "articleTag":{
        "type": "keyword",
        "copy_to": "all"
      },
      "all":{
        "type": "text",
        "analyzer": "text_anlyzer",
        "search_analyzer": "ik_smart"
      },
      "suggestion":{
          "type": "completion",
          "analyzer": "completion_analyzer",  
          "search_analyzer":"ik_smart"
      }
    }
  }
}
```

