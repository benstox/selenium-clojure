(ns selenium-clojure.core
  (:require [environ.core :refer [env]]
            [clj-webdriver.core :as cw]
            [clj-webdriver.wait :as wait]))


; use this Java command to specify the location of the Chrome driver
(System/setProperty "webdriver.chrome.driver" (env :chrome-driver-path))

; open the browser 
(def driver (cw/new-driver {:browser :chrome}))
  (wait/implicit-wait driver 5000)

(defn assert-and-click
  "Assert that an element exists and then click on it."
  [css text]
  (let [element (cw/find-element driver {:css css})]
    (assert element)
    (assert (= text (cw/text element)))
    (cw/click element)))

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


(defn -main
  "Go test my website!"
  [& args]

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

  ; (assert (cw/find-element driver {:css "#review-count"}))
  ; (def article-count-init (get-review-count))
  ; (println (str "Initial article count: " article-count-init))
  ; (Thread/sleep 1500)
  
  ; ; review an article (interesting)
  ; (println (str "Reviewing entry PK: " (get-entry-pk)))
  ; (assert-and-click "#review-buttons > div:nth-of-type(1)" "Interesting")
  ; (Thread/sleep 1500)
  ; (def article-count-review-1 (get-review-count))
  ; (println "Reviewed an article as interesting")
  ; (println (str "New article count: " article-count-review-1))
  ; (assert (= (dec article-count-init) article-count-review-1))

  ; ; review an article (not interesting)
  ; (println (str "Reviewing entry PK: " (get-entry-pk)))
  ; (assert-and-click "#review-buttons > div:nth-of-type(2)" "Not interesting!")
  ; (Thread/sleep 1500)
  ; (def article-count-review-2 (get-review-count))
  ; (println "Reviewed an article as not interesting")
  ; (println (str "New article count: " article-count-review-2))
  ; (assert (= (dec article-count-review-1) article-count-review-2))
  
  ; ; review an article (non-uk)
  ; (println (str "Reviewing entry PK: " (get-entry-pk)))
  ; (assert-and-click "#review-buttons > div:nth-of-type(3)" "Non-UK deal")
  ; (Thread/sleep 1500)
  ; (def article-count-review-3 (get-review-count))
  ; (println "Reviewed an article as non-UK")
  ; (println (str "New article count: " article-count-review-3))
  ; (assert (= (dec article-count-review-2) article-count-review-3))
  
  ; ; skip an article
  ; (println (str "Reviewing entry PK: " (get-entry-pk)))
  ; (assert-and-click "#skip-article" "Skip this one")
  ; (Thread/sleep 1500)
  ; (def article-count-skipped (get-review-count))
  ; (println "Skipped an article.")
  ; (println (str "New article count: " article-count-skipped))
  ; (assert (= article-count-review-3 article-count-skipped))


  ; go to Night Judge
  (assert (cw/find-element driver {:tag "input" :value "frances2" :checked "checked"}))
  (cw/click (cw/find-element driver {:tag "input" :value "night-judge"}))
  
  ; sometimes takes a while for the Beauhurst data to get sent over
  (wait/wait-until driver (fn [& args] (boolean (cw/find-element driver {:css "#night-judge-logo > div > img"}))) 60000)
  
  (def nj-article-count-init (get-review-count))
  (println (str "Night Judge article count: " nj-article-count-init))
  (assert (cw/find-element driver {:css "#article-text > div span.highlighted"}))
  (Thread/sleep 1500)

  ; review an article (relevant and interesting)
  (println (str "Reviewing entry PK: " (get-entry-pk)))
  (assert-and-click "#review-buttons > div:nth-of-type(1)" "Relevant and interesting!")
  (Thread/sleep 1500)
  (def nj-article-count-review-1 (get-review-count))
  (println "Reviewed an article as relevant and interesting")
  (println (str "New article count: " nj-article-count-review-1))
  (assert (= (dec nj-article-count-init) nj-article-count-review-1))

  ; review an article (relevant, not interesting)
  (println (str "Reviewing entry PK: " (get-entry-pk)))
  (assert-and-click "#review-buttons > div:nth-of-type(2)" "Relevant but not interesting")
  (Thread/sleep 1500)
  (def nj-article-count-review-2 (get-review-count))
  (println "Reviewed an article as relevant but not interesting")
  (println (str "New article count: " nj-article-count-review-2))
  (assert (= (dec nj-article-count-review-1) nj-article-count-review-2))
  
  ; review an article (not relevant)
  (println (str "Reviewing entry PK: " (get-entry-pk)))
  (assert-and-click "#review-buttons > div:nth-of-type(3)" "Not relevant!")
  (Thread/sleep 1500)
  (def nj-article-count-review-3 (get-review-count))
  (println "Reviewed an article as not relevant")
  (println (str "New article count: " nj-article-count-review-3))
  (assert (= (dec nj-article-count-review-2) nj-article-count-review-3))
  
  ; skip an article
  (println (str "Reviewing entry PK: " (get-entry-pk)))
  (assert-and-click "#skip-article" "Skip this one")
  (Thread/sleep 1500)
  (def nj-article-count-skipped (get-review-count))
  (println "Skipped an article.")
  (println (str "New article count: " nj-article-count-skipped))
  (assert (= nj-article-count-review-3 nj-article-count-skipped))

  )

