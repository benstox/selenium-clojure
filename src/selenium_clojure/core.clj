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

(defn review-article
  ([button-text css last-count]
    (review-article button-text css last-count identity))
  ([button-text css last-count new-count-modifier]
    (println (str "Reviewing entry PK: " (get-entry-pk)))
    (assert-and-click css button-text)
    (Thread/sleep 1500)
    (let [new-count (get-review-count)]
      (if (re-find #"[Ss]kip" button-text)
        (println "Skipped an article.")
        (println (str "Reviewed an article as " button-text ".")))
      (println (str "New article count: " new-count))
      (assert (= (new-count-modifier last-count) new-count))
      new-count)))

(defn -main
  "Go test my website!"
  [& args]

  ; wait a moment
  (Thread/sleep 3000)

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

  (assert (cw/find-element driver {:css "#review-count"}))
  (def article-count (get-review-count))
  (println (str "Initial article count: " article-count))
  (Thread/sleep 1500)
  
  ; review an article (interesting)
  (def article-count
    (review-article "Interesting" "#review-buttons > div:nth-of-type(1)" article-count dec))

  ; review an article (not interesting)
  (def article-count
    (review-article "Not interesting!" "#review-buttons > div:nth-of-type(2)" article-count dec))
  
  ; review an article (non-uk)
  (def article-count
    (review-article "Non-UK deal" "#review-buttons > div:nth-of-type(3)" article-count dec))
  
  ; skip an article
  (def article-count
    (review-article "Skip this one" "#skip-article" article-count))


  ; go to Night Judge
  (assert (cw/find-element driver {:tag "input" :value "frances2" :checked "checked"}))
  (cw/click (cw/find-element driver {:tag "input" :value "night-judge"}))
  
  ; sometimes takes a while for the Beauhurst data to get sent over
  (wait/wait-until driver (fn [& args] (boolean (cw/find-element driver {:css "#night-judge-logo > div > img"}))) 120000)
  
  (def article-count (get-review-count))
  (println (str "Night Judge article count: " article-count))
  (assert (cw/find-element driver {:css "#article-text > div span.highlighted"}))
  (assert (cw/find-element driver {:css "#night-judge-companies > div.well"}))
  (Thread/sleep 1500)

  ; review an article (relevant and interesting)
  (def article-count
    (review-article "Relevant and interesting!" "#review-buttons > div:nth-of-type(1)" article-count dec))

  ; review an article (relevant, not interesting)
  (def article-count
    (review-article "Relevant but not interesting" "#review-buttons > div:nth-of-type(2)" article-count dec))
  
  ; review an article (not relevant)
  (def article-count
    (review-article "Not relevant!" "#review-buttons > div:nth-of-type(3)" article-count dec))
  
  ; skip an article
  (def article-count
    (review-article "Skip this one" "#skip-article" article-count))

  
  ; go to Judge Frances
  (cw/click (cw/find-element driver {:tag "input" :value "judge-frances"}))
  (assert (or
      (cw/find-element driver {:css "#night-judge-logo > div > img"})
      (cw/find-element driver {:css "h4.frances-quote"})))
  
  (Thread/sleep 1500)
  
  (def article-count (get-review-count))
  (println (str "Judge Frances article count: " article-count))
  (Thread/sleep 1500)

  (doseq [f2i (map inc (range 3))
        nji (map inc (range 3))]
    (let [frances-2 (boolean (cw/find-element driver {:css "h4.frances-quote"}))
          night-judge (boolean (cw/find-element driver {:css "#night-judge-logo > div > img"}))]
      (println (str "Frances 2 " frances-2))
      (println (str "Night Judge " night-judge))
      (cond
        (and frances-2 night-judge)
          (let [f2-button (cw/find-element driver {:css (str "#f2-radio > label:nth-of-type(" f2i ")")})
                nj-button (cw/find-element driver {:css (str "#nj-radio > label:nth-of-type(" nji ")")})
                review-button (cw/find-element driver {:css "#review-buttons > div.f2njreview"})
                last-review-count article-count]
            (cw/click f2-button)
            (cw/click nj-button)
            (cw/click review-button)
            (Thread/sleep 1500)
            (def article-count (get-review-count))
            (println (str "New article count: " article-count))
            (assert (= (dec last-review-count) article-count)))
        frances-2
          (let [button (cw/find-element driver {:css (str "#review-buttons > div:nth-of-type(" f2i ")")})
                last-review-count article-count]
            (cw/click button)
            (Thread/sleep 1500)
            (def article-count (get-review-count))
            (println (str "New article count: " article-count))
            (assert (= (dec last-review-count) article-count)))
        night-judge
          (let [button (cw/find-element driver {:css (str "#review-buttons > div:nth-of-type(" nji ")")})
                last-review-count article-count]
            (cw/click button)
            (Thread/sleep 1500)
            (def article-count (get-review-count))
            (println (str "New article count: " article-count))
            (assert (= (dec last-review-count) article-count))))))

  ; go to Portfolio Checker
  (cw/click (cw/find-element driver {:css "body > nav > div > ul:nth-child(2) > li:nth-of-type(1)"}))
  (cw/click (cw/find-element driver {:text "Portfolios"}))

  (def article-count (get-review-count))
  (def article-count
    (review-article "Interesting" "#review-buttons > div:nth-of-type(1)" article-count dec))
  (def article-count
    (review-article "Not interesting!" "#review-buttons > div:nth-of-type(2)" article-count dec))
  (def article-count
    (review-article "Skip this one" "#skip-article" article-count))

  ; switch to equity portfolios
  (cw/click (cw/find-element driver {:tag "input" :value "equity"}))

  (Thread/sleep 500)
  (def article-count (get-review-count))
  (def article-count
    (review-article "Interesting" "#review-buttons > div:nth-of-type(1)" article-count dec))
  (def article-count
    (review-article "Not interesting!" "#review-buttons > div:nth-of-type(2)" article-count dec))
  (def article-count
    (review-article "Skip this one" "#skip-article" article-count))
  (assert (re-find #"Equity" (cw/text (cw/find-element driver {:css "#article-text > div:nth-of-type(3)"}))))

  ; switch to grant portfolios
  (cw/click (cw/find-element driver {:tag "input" :value "grant"}))

  (Thread/sleep 500)
  (def article-count (get-review-count))
  (def article-count
    (review-article "Interesting" "#review-buttons > div:nth-of-type(1)" article-count dec))
  (def article-count
    (review-article "Not interesting!" "#review-buttons > div:nth-of-type(2)" article-count dec))
  (def article-count
    (review-article "Skip this one" "#skip-article" article-count))
  (assert (re-find #"Grant" (cw/text (cw/find-element driver {:css "#article-text > div:nth-of-type(3)"}))))

  ; switch to accelerator portfolios
  (cw/click (cw/find-element driver {:tag "input" :value "accelerator"}))

  (Thread/sleep 500)
  (def article-count (get-review-count))
  (def article-count
    (review-article "Interesting" "#review-buttons > div:nth-of-type(1)" article-count dec))
  (def article-count
    (review-article "Not interesting!" "#review-buttons > div:nth-of-type(2)" article-count dec))
  (def article-count
    (review-article "Skip this one" "#skip-article" article-count))
  (assert (re-find #"Accelerator" (cw/text (cw/find-element driver {:css "#article-text > div:nth-of-type(3)"}))))

  ; quit
  (println "All the tests passed! Goodbye!")
  (Thread/sleep 5000)
  (cw/quit driver))
