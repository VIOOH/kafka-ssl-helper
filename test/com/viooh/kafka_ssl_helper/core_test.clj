;;
;; Copyright 2019-2020 VIOOH Ltd
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;;     http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.
;;
(ns com.viooh.kafka-ssl-helper.core-test
  (:require [midje.sweet :refer :all]
            [midje.util :refer :all]
            [com.viooh.kafka-ssl-helper.core :as sut]))

(testable-privates com.viooh.kafka-ssl-helper.core
                   random-password)



(def pass "fake_password")



(fact "Can generate passwords"
  (pos? (count (random-password 4)))
  => true)



(fact "Return correct correct keys"
  (sut/ssl-opts {:cert-pem    :cert-pem
                 :private-key :private-key
                 :ca-cert-pem :ca-cert})
  =>
  {:ssl.keystore.location   "ks.jks"
   :ssl.keystore.password   pass
   :ssl.truststore.location "ts.jks"
   :ssl.truststore.password pass
   :security.protocol       "SSL"}

  ;; checking interactions
  (provided (#'sut/random-password anything) => pass)
  (provided (#'sut/set-pk+cert anything pass anything anything) => :ks)
  (provided (#'sut/set-rootca-cert anything pass anything) => :ts)

  (provided (#'sut/persist-temp-ks :ks anything pass) => "ks.jks")
  (provided (#'sut/persist-temp-ks :ts anything pass) => "ts.jks"))



(fact "Truststore is only generated when a ca-cert-pem is passed"
  (sut/ssl-opts {:cert-pem    :cert-pem
                 :private-key :private-key})
  =>
  {:ssl.keystore.location   "ks.jks"
   :ssl.keystore.password   pass
   :security.protocol       "SSL"}

  ;; checking interactions
  (provided (#'sut/random-password anything) => pass)
  (provided (#'sut/set-pk+cert anything pass anything anything) => :ks)

  (provided (#'sut/persist-temp-ks :ks anything pass) => "ks.jks"))



(fact "Keystore is only generated when a a private key & certificated  are passed"
  (sut/ssl-opts {:ca-cert-pem :ca-cert})
  =>
  {:ssl.truststore.location "ts.jks"
   :ssl.truststore.password pass
   :security.protocol       "SSL"}

  ;; checking interactions
  (provided (#'sut/random-password anything) => pass)
  (provided (#'sut/set-rootca-cert anything pass anything) => :ts)

  (provided (#'sut/persist-temp-ks :ts anything pass) => "ts.jks"))
