(ns pass-android.android.password-list
  (:require-macros [rum.core :refer [defc]])
  (:require [re-natal.support :as support]
            [pass-android.android.utils :refer [create-element hex-alph]]
            [goog.crypt.base64 :as b64]
            [rum.core :as rum]))

(def fs (js/require "react-native-fs"))
(def ReactNative (js/require "react-native"))
(def Crypto (js/require "crypto-js"))
(def ActionButton (.-default (js/require "react-native-action-button")))

(def action-button (partial create-element ActionButton))
(def view (partial create-element (.-View ReactNative)))
(def text (partial create-element (.-Text ReactNative)))
(def image (partial create-element (.-Image ReactNative)))
(def flat-list (partial create-element (.. ReactNative -FlatList)))
(def clipboard (.-Clipboard ReactNative))
(def toast (.-ToastAndroid ReactNative))
(def touchable-native-feedback (partial create-element (.-TouchableNativeFeedback ReactNative)))
(def selectable-background (.. ReactNative -TouchableNativeFeedback SelectableBackground))

(defn refresh-files [state]
  (let [m @state
        folder (get m :folder)
        p-read (.readDir fs folder)]
    (swap! state assoc :refreshing true)
    (-> p-read
        (.then #(swap! state assoc :files
                       (->> (js->clj % :keywordize-keys true)
                            (filter (fn [x] ((get x :isFile))))
                            (mapv (fn [{:keys [path name]}]
                                    (let [fancy-name name]
                                      {:path path
                                       :key name
                                       :name fancy-name
                                       :filename name}))))
                       :refreshing false))
        (.catch #(swap! state assoc :refreshing false)))))

(defc render-row
  [state x]
  (let [item (js->clj (.-item x) :keywordize-keys true)
        {:keys [path name]} item]
    (touchable-native-feedback {:background selectable-background
                                :onPress #(swap! state assoc
                                                 :file-to-decrypt path
                                                 :file-to-decrypt-name name)}
                               (view {:style {:padding 20
                                              :borderBottomWidth 1}}
                                     (text name)))))

(defc no-file-placeholder
  [state]
  (view {:style {:flex 1
                 :justifyContent "center"
                 :alignItems "center"}}
        (text {:style {:opacity 0.5
                       :fontSize 30}}
              "No passfiles found :(")
        (touchable-native-feedback {:background selectable-background
                                    :onPress #(refresh-files state)}
                                   (view {:elevation 5
                                          :style {:backgroundColor "#009688"
                                                  :borderRadius 5
                                                  :marginTop 30
                                                  :padding 10}}
                                         (text {:style {:color "white"
                                                        :fontSize 20
                                                        :textAlign "center"}}
                                               "Refresh")))))

(defc password-list-header
  [state]
  (view {:style {:padding 20
                 :backgroundColor "#009688"}
         :elevation 10}

        (text {:style {:fontSize 25
                       :color "white"}}
              "Pass3301")))
(defc password-list-page
  [state]
  (let [m @state
        files (get m :files [])
        folder (get m :folder [])
        refreshing (get m :refreshing false)]
    (view {:style {:flexDirection "column"
                   :flex 1}}
          (password-list-header state)
          (if (empty? files)
            (no-file-placeholder state)
            (flat-list {:data files
                        :onRefresh #(refresh-files state)
                        :refreshing refreshing
                        :renderItem #(render-row state %)}))
          (action-button {:buttonColor "#009688"
                          :size 60
                          :onPress #()}))))
