(ns selenium-clojure.core
  (:require [environ.core :refer [env]]
            [clj-webdriver.core :as cw]))


(defn implicit-wait
  "The clj-webdriver.core.implicit-wait function doesn't seem
  to get loaded for some reason, so I've just pasted the code here."
  [wd timeout]
  (.implicitlyWait (.. (:webdriver wd) manage timeouts) timeout java.util.concurrent.TimeUnit/MILLISECONDS)
  wd)


(defn assert-and-click-closure
  "Assert that an element exists and then click on it."
  [css text driver]
  (let [element (cw/find-element driver {:css css})]
    (assert element)
    (assert (= text (cw/text element)))
    (cw/click element)))


(defn -main
  "Go test my website!"
  [& args]

  ; use this Java command to specify the location of the Chrome driver
  (System/setProperty "webdriver.chrome.driver" (env :chrome-driver-path))

  ; open the browser 
  (def driver (cw/new-driver {:browser :chrome}))
  (implicit-wait driver 5000)

  (defn assert-and-click
    "Closure of above."
    [css text]
    (assert-and-click-closure css text driver))

  ; go to the website
  (cw/to driver (env :url))
  (assert (re-find #"Engine Room" (cw/title driver)))

  ; click log in
  (assert-and-click "ul.navbar-right > li:nth-of-type(2) > a" "Log In")
  
  ; enter details
  (let [username-input (cw/find-element driver {:css "#id_login"})
        password-input (cw/find-element driver {:css "#id_password"})]
    (assert username-input)
    (assert password-input)
    (cw/input-text username-input (env :username))
    (cw/input-text password-input (env :password)))

  ; click sign in
  (assert-and-click "form.login > button.btn-primary" "Sign In")
  
  ; check if Frances 2, Night Judge and Portfolio are here
  (assert (re-find #"Origination Station" (cw/text (cw/find-element driver {:css "#main > div.row > div:nth-of-type(1)"}))))
  (assert (re-find #"Frances 2" (cw/text (cw/find-element driver {:css "#main > div.row > div:nth-of-type(1)"}))))
  (assert (re-find #"Night Judge" (cw/text (cw/find-element driver {:css "#main > div.row > div:nth-of-type(1)"}))))
  (assert (re-find #"Portfolios" (cw/text (cw/find-element driver {:css "#main > div.row > div:nth-of-type(1)"}))))

  (let [dropdown (cw/find-element driver {:css "body > nav > div > ul:nth-child(2) > li:nth-of-type(1)"})]
    (cw/click dropdown)
    (assert (re-find #"Origination Station" (cw/text dropdown)))
    (assert (re-find #"Frances 2" (cw/text dropdown)))
    (assert (re-find #"Night Judge" (cw/text dropdown)))
    (assert (re-find #"Portfolios" (cw/text dropdown))))

  
  ; go to Frances 2
  (cw/click (cw/find-element driver {:text "Frances 2"}))

  (defn get-review-count
    "Get the review count number from the Judge Frances pages."
    []
    (let [div (cw/find-element driver {:css "#review-count"})
          text (cw/text div)
          articles (Integer/parseInt (re-find #"\d+" text))]
      articles))

  (defn get-entry-pk
    "Get the PK of the currently displayed NewsEntry from the Judge Frances pages."
    []
    (let [input (cw/find-element driver {:css "#id_entry_pk"})
          value (cw/value input)]
      value))

  (assert (cw/find-element driver {:css "#review-count"}))
  (def article-count-init (get-review-count))
  (println (str "Initial article count: " article-count-init))
  
  ; review an article (interesting)
  (println (str "Reviewing entry PK: " (get-entry-pk)))
  (assert-and-click "#review-buttons > div:nth-of-type(1)" "Interesting")
  (Thread/sleep 1500)
  (def article-count-review-1 (get-review-count))
  (println "Reviewed an article as interesting")
  (println (str "New article count: " article-count-review-1))
  (assert (= (dec article-count-init) article-count-review-1))

  ; review an article (not interesting)
  (println (str "Reviewing entry PK: " (get-entry-pk)))
  (assert-and-click "#review-buttons > div:nth-of-type(2)" "Not interesting!")
  (Thread/sleep 1500)
  (def article-count-review-2 (get-review-count))
  (println "Reviewed an article as not interesting")
  (println (str "New article count: " article-count-review-2))
  (assert (= (dec article-count-review-1) article-count-review-2))
  
  ; review an article (non-uk)
  (println (str "Reviewing entry PK: " (get-entry-pk)))
  (assert-and-click "#review-buttons > div:nth-of-type(3)" "Non-UK deal")
  (Thread/sleep 1500)
  (def article-count-review-3 (get-review-count))
  (println "Reviewed an article as non-UK")
  (println (str "New article count: " article-count-review-3))
  (assert (= (dec article-count-review-2) article-count-review-3))
  
  ; skip an article
  (println (str "Reviewing entry PK: " (get-entry-pk)))
  (assert-and-click "#review-buttons > div:nth-of-type(4)" "Skip this one")
  (Thread/sleep 1500)
  (def article-count-skipped (get-review-count))
  (println "Skipped an article.")
  (println (str "New article count: " article-count-skipped))
  (assert (= article-count-review-3 article-count-skipped))

  
  ; go to Night Judge
  (assert (cw/find-element driver {:tag "input" :value "frances2" :checked "checked"}))
  (cw/click (cw/find-element driver {:tag "input" :value "night-judge"}))
  )

