(ns pass-android.android.utils)

(defn create-element [& args]
  (if (or (= (type {})
             (type (second args)))
          (= (type (zipmap (range 24) (range 24)))
             (type (second args))))
    (let [[rn-comp opts & children] args]
      (apply js/React.createElement rn-comp (clj->js opts) children))
    (apply create-element (first args) {} (rest args))))

(def hex-alph {\0 0 \1 1 \2 2 \3 3 \4 4 \5 5 \6 6 \7 7 \8 8 \9 9 \a 10 \b 11 \c 12 \d 13 \e 14 \f 15})

(def Crypto (js/require "crypto-js"))

(defn encrypt [plain k]
  (.. Crypto -AES (encrypt plain k)))
(defn decrypt [enc k]
  (.. Crypto -AES (decrypt enc k)))
