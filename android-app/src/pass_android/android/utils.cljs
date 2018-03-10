(ns pass-android.android.utils
  (:require [clojure.string :refer [lower-case]]))

(def ReactNative (js/require "react-native"))

(def async-storage (.-AsyncStorage ReactNative))
(def set-item (fn [k v] (.setItem async-storage k v)))
(def get-item (fn [k] (.getItem async-storage k)))

(defn create-element [& args]
  (if (map? (second args))
    (let [[rn-comp opts & children] args]
      (apply js/React.createElement rn-comp (clj->js opts) children))
    (apply create-element (first args) {} (rest args))))

(def hex-alph {\0 0 \1 1 \2 2 \3 3 \4 4 \5 5 \6 6 \7 7 \8 8 \9 9 \a 10 \b 11 \c 12 \d 13 \e 14 \f 15})

(def Crypto (js/require "crypto-js"))

(defn encrypt [plain k]
  (.. Crypto -AES (encrypt plain k)))
(defn decrypt [enc k]
  (.. Crypto -AES (decrypt enc k)))

(defn string-matches
  [string pattern]
  (cond
    (empty? pattern) true
    (empty? string) false
    :else (if (= (lower-case (first string))
                 (lower-case (first pattern)))
            (recur (rest string) (rest pattern))
            (recur (rest string) pattern))))
