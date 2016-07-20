package com.locusintellect

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class WebCrawlerApplication {

    public static void main(String[] args) {
        def applicationContext = SpringApplication.run(WebCrawlerApplication, args)
        def crawlerController = applicationContext.getBean(WebCrawlerHandler)
        crawlerController.handle()
        applicationContext.close()
    }

}
