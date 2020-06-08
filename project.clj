(defproject com.viooh/kafka-ssl-helper "0.9.0"
  :description "Temporary trust/keystores for your kafka apps"
  :url "https://github.com/VIOOH/kafka-ssl-helper"

  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :dependencies [[org.clojure/clojure "1.10.0"]
                 [xsc/pem-reader "0.1.1"]]
  :repl-options {:init-ns com.viooh.kafka-ssl-helper.core}
  :profiles {:dev
             {:plugins [[lein-midje "3.2"]]
              :dependencies [[midje/midje "1.9.8"]]}}

  :java-source-paths ["java/src"]
  :javac-options     ["-target" "1.8" "-source" "1.8"]

  )
