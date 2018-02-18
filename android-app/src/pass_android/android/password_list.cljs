(ns pass-android.android.password-list
  (:require-macros [rum.core :refer [defc]])
  (:require [re-natal.support :as support]
            [pass-android.android.utils :refer [create-element hex-alph]]
            [goog.crypt.base64 :as b64]
            [rum.core :as rum]))

(def fs (js/require "react-native-fs"))
(def ReactNative (js/require "react-native"))
(def Crypto (js/require "crypto-js"))


(def view (partial create-element (.-View ReactNative)))
(def text (partial create-element (.-Text ReactNative)))
(def image (partial create-element (.-Image ReactNative)))
(def flat-list (partial create-element (.. ReactNative -FlatList)))
(def clipboard (.-Clipboard ReactNative))
(def toast (.-ToastAndroid ReactNative))
(def touchable-native-feedback (partial create-element (.-TouchableNativeFeedback ReactNative)))
(def selectable-background (.. ReactNative -TouchableNativeFeedback SelectableBackground))

(defn encrypt [plain k]
  (.. Crypto -AES (encrypt plain k)))
(defn decrypt [enc k]
  (.. Crypto -AES (decrypt enc k)))

(defn on-press [state]
  (let [m @state
        folder (get m :folder)
        p-read (.readDir fs folder)]


    (-> p-read
        (.then #(swap! state assoc :files
                       (->> (js->clj % :keywordize-keys true)

                            (filter (fn [x] ((get x :isFile))))
                            (mapv (fn [{:keys [path name]}] {:path path
                                                             :key name
                                                             :name name})))))
        (.catch #(println "ERR" %)))))

;; (.then (.readFile fs path "utf8")
;;        (fn [d] (let [x (-> d
;;                            (decrypt "test")
;;                            str)
;;                      mesg (if (zero? (count x))
;;                             "Error"
;;                             (->> x
;;                                  (partition 2)
;;                                  butlast
;;                                  (map (fn [[a b]] (+ (* 16 (hex-alph a))
;;                                                      (hex-alph b))))
;;                                  (map char)
;;                                  (apply str)))]
;;                  (.show toast mesg (.-SHORT toast)))))


(defc render-row
  [state item]
  (let [{:keys [path name]} item]
    (touchable-native-feedback {:background selectable-background
                                :onPress #(swap! state assoc :file-to-decrypt name)}
                               (view {:style {:padding 20
                                              :borderTopWidth 1}}
                                     (text name)))))

(defc no-file-placeholder
  [state]
  (view {:flex 1
         :style {:justifyContent "center"
                 :alignItems "center"}}
        (text "No passfiles found :(")))

(defc password-list-page
  [state]
  (let [m @state
        files (get m :files [])
        folder (get m :folder [])]
    (view {:style {:flexDirection "column"
                   :marginTop 40
                   :flex 1}}
          (text {:style {:fontSize 30
                         :fontWeight "100"
                         :marginBottom 10
                         :textAlign "center"}}
                "Password Manager")
          (text {:style {:marginBottom 10
                         :textAlign "center"}}
                folder)
          (view {:style {:justifyContent "space-around"
                         :flexDirection "row"
                         :marginLeft 20
                         :marginRight 20
                         :marginBottom 10}}
                ;; (touchable-native-feedback {:background selectable-background
                ;;                             :onPress #(on-press state)}
                ;;                            (view {:style {:backgroundColor "#999"
                ;;                                           :padding 10
                ;;                                           :borderRadius 5}}
                ;;                                  (text {:style {:color "white"
                ;;                                                 :textAlign "center"
                ;;                                                 :fontWeight "bold"}}
                ;;                                        "Get pass list")))
                (touchable-native-feedback {:background selectable-background
                                            :onPress #(on-press state)}
                                           (view {:style {:backgroundColor "#999"
                                                          :padding 10
                                                          :borderRadius 5}}
                                                 (text {:style {:color "white"
                                                                :textAlign "center"
                                                                :fontWeight "bold"}}
                                                       "Get pass list"))))
          (if (empty? files)
            (no-file-placeholder state)
            (view {:style {:borderBottomWidth 1}}
                  (flat-list {:data files
                              :renderItem (fn [x] (let [item (js->clj (.-item x) :keywordize-keys true)]
                                                    (render-row state item)))}))))))
