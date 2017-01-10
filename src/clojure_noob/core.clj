(ns clojure-noob.core
  (:gen-class)
  (:require [clojure.java.io :as io])
  (:require [http.async.client :as http])
)

(defn parse-html [content]
  "Gets list of image ids from wallhaven html body"
  (map second (re-seq #"https://alpha.wallhaven.cc/wallpaper/(\d+)" content))
)

(defn make-img-url [id]
  (str "https://wallpapers.wallhaven.cc/wallpapers/full/wallhaven-" id ".jpg")
)

(defn make-output-path [root id]
  (str root "/wall_" id ".jpg"))

(defn get-img [client id]
  (println (str "getting " id))
  (let [response (http/GET client (make-img-url id))]
    (-> response
        http/await
        http/body
        .toByteArray)))

(defn get-image-ids [client url]
  (let [response (http/GET client url)]
    (-> response
        http/await
        http/string
        parse-html)))

(defn save-img [binaryimage file]
  (with-open [out (io/output-stream file)]
    (io/copy binaryimage out)))

(defn -main
  [url & args]
  (with-open [client (http/create-client)]
    (def id-list (take 9 (set (get-image-ids client url))))
    (def images (doall (map (partial get-img client) id-list))))
  (doall (for [[image id] (map vector images (range 1 10))]
     (save-img image (make-output-path "/home/eric/.wall" id))))
)
