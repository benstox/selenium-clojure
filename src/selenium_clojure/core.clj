(ns selenium-clojure.core
  (:require [environ.core :refer [env]]
            [clj-webdriver.taxi :as tx]))

; (defn new-chrome-browser []
;   (cw/start {:browser :chrome} (env :url)))

(defn -main
  "Go test my website!"
  [& args]

  ; use this Java command to specify the locatino of the Chrome driver
  (System/setProperty "webdriver.chrome.driver" (env :chrome-driver-path))

  ; open the browser
  (tx/set-driver! {:browser :chrome})
  (tx/implicit-wait 10000)
  
  ; go to the website
  (tx/to (env :url))
  (assert (re-find #"Engine Room" (tx/title)))
