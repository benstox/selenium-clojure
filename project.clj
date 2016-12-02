(defproject selenium-clojure "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [environ "1.1.0"]
                 [clj-webdriver "0.7.2"]
                 [org.seleniumhq.selenium/selenium-java "3.0.1"]
                 [org.seleniumhq.selenium/htmlunit-driver "2.23.1" :exclusions [org.seleniumhq.selenium/selenium-support]]
                 [org.seleniumhq.selenium/selenium-support "3.0.1"]]
  :main ^:skip-aot selenium-clojure.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
