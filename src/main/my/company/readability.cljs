(ns my.company.readability
  (:require
    ["@mozilla/readability" :refer (Readability)]
    ["https" :as https]
    ["jsdom" :refer (JSDOM)]))

(defn handler [event]
  (js/console.log "handler called")
  (js/Promise.
    (fn [resolve reject]
      ;; FIXME: actually take the url to get from event
      (let [url "https://code.thheller.com"]
        (js/console.log "get started")
        (-> (https/get url
              (fn [^js result]
                (js/console.log "get completed" (.-statusCode result))
                (if (not= 200 (.-statusCode result))
                  (resolve ;; reject?
                    #js {:statusCode (.-statusCode result)
                         :body "exected 200, but didn't get it"})

                  ;; success
                  (let [content-ref (atom "")]
                    (.setEncoding result "utf-8")
                    (.on result "data"
                      (fn [chunk]
                        (js/console.log "chunk")
                        (swap! content-ref str chunk)))
                    (.on result "end"
                      (fn []
                        (js/console.log "end" (count @content-ref))
                        (try
                          (let [content @content-ref
                                doc (JSDOM. content #js {:url url})
                                reader (Readability. (.. doc -window -document))
                                article (.parse reader)
                                response #js {:statusCode 200 :body (js/JSON.stringify article)}]
                            (js/console.log "done")
                            (resolve response))
                          (catch :default e
                            (reject e))))))
                  )))
            (.on "error" reject))))))
