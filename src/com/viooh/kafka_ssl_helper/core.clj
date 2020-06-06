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
(ns com.viooh.kafka-ssl-helper.core
  (:import [java.security KeyStore]
           [java.security.cert Certificate CertificateFactory]
           [java.security SecureRandom])
  (:require [pem-reader.core :as pem]
            [clojure.string  :as str]
            [clojure.java.io :as io]))



(defn- random-password
  "Number of random integers generated for the password"
  [n]
  (let [rdm (SecureRandom.)]
    (->> (repeatedly #(.nextInt rdm))
      (take n)
      (map (partial format "%x"))
      (str/join ""))))



(defn- ^KeyStore empty-keystore
  "Generate"
  [pass]
  (doto (KeyStore/getInstance "PKCS12")
    (.load nil (char-array pass))))



(defn- certificate-factory
  []
  (CertificateFactory/getInstance "X509"))



(defn- load-key
  [^String key]
  (-> key
    (.getBytes "UTF-8")
    io/input-stream
    pem/read
    pem/private-key))



(defn- set-pk+cert
  [^KeyStore ks ks-pass ^String private-key ^String cert-pem]
  (let [^CertificateFactory cert-factory (certificate-factory)
        key   (load-key private-key)
        certs (.generateCertificates cert-factory
                                     (io/input-stream (.getBytes cert-pem "UTF-8")))]
    (.setKeyEntry ks
                  "private" key
                  (char-array ks-pass)
                  (into-array Certificate certs)))
  ks)



(defn- set-rootca-cert
  [^KeyStore ks ks-pass ^String cert-pem]
  (let [^CertificateFactory cert-factory (certificate-factory)
        cert (.generateCertificate cert-factory
                                   (io/input-stream (.getBytes cert-pem "UTF-8")))]
    (.setCertificateEntry ks "CARoot" cert))
  ks)



(defn- persist-ks
  [^KeyStore ks ^String dest ks-pass]
  (with-open [f (java.io.FileOutputStream. dest)]
    (.store ks f (char-array ks-pass))))



(defn- tmp-file
  [name extension]
  (let [tmp-file (java.io.File/createTempFile name extension)]
    (.deleteOnExit tmp-file)
    (.getAbsolutePath tmp-file)))



(defn- persist-temp-ks
  [^KeyStore ks name ks-pass]
  (let [tmp-file (tmp-file name ".jks")]
    (persist-ks ks tmp-file ks-pass)
    tmp-file))



(defn- truststore-ssl-opts
  [ca-cert-pem]
  (when ca-cert-pem
    (let [ts-pass (random-password 4)
          ;; insert the ca cert in the truststore
          ts      (-> (empty-keystore ts-pass)
                    (set-rootca-cert ts-pass ca-cert-pem))]
      {:ssl.truststore.location (persist-temp-ks ts "temp_truststore" ts-pass)
       :ssl.truststore.password ts-pass})))



(defn keystore-ssl-opts
  "Return valid configuration options for a kafka consumer using ssl auth"
  [{:keys [cert-pem ca-cert-pem private-key] :as opts}]
  (when (and cert-pem private-key)
    (let [ks-pass (random-password 4)
          ;; insert the pk and the cert in the keystore
          ks      (-> (empty-keystore ks-pass)
                    (set-pk+cert ks-pass private-key cert-pem))]

      (merge
       (truststore-ssl-opts ca-cert-pem)
       {:ssl.keystore.location   (persist-temp-ks ks "temp_keystore" ks-pass)
        :ssl.keystore.password   ks-pass}))))



(defn ssl-opts
  "Return valid configuration options for a kafka consumer using ssl auth"
  [{:keys [cert-pem ca-cert-pem private-key] :as opts}]
  (merge
   (truststore-ssl-opts ca-cert-pem)
   (keystore-ssl-opts opts)
   {:security.protocol       "SSL"}))
