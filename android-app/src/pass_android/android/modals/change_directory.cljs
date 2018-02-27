(ns pass-android.android.modals.change-directory
  (:require-macros [rum.core :refer [defc]])
  (:require [re-natal.support :as support]
            [pass-android.android.utils :refer [create-element hex-alph decrypt]]
            [pass-android.android.actions :refer [dispatch!]]
            [clojure.string :refer [join]]
            [goog.crypt.base64 :as b64]
            [rum.core :as rum]))

(def ReactNative (js/require "react-native"))
(def fs (js/require "react-native-fs"))
(def MaterialIcons (js/require "react-native-vector-icons/MaterialIcons"))

(def icon (partial create-element (.-default MaterialIcons)))
(def view (partial create-element (.-View ReactNative)))
(def text (partial create-element (.-Text ReactNative)))
(def modal (partial create-element (.-Modal ReactNative)))
(def text-input (partial create-element (.-TextInput ReactNative)))
(def toast (.-ToastAndroid ReactNative))
(def touchable-opacity (partial create-element (.-TouchableOpacity ReactNative)))
(def flat-list (partial create-element (.. ReactNative -FlatList)))
(def touchable-native-feedback (partial create-element (.-TouchableNativeFeedback ReactNative)))

(defc change-directory-header
  [state]
  (let [m @state
        folder (->> (or (get-in m [:change-directory :directory])
                        (get m :folder))
                    rest
                    (join "/")
                    (str "/"))]
    (view {:style {:paddingLeft 15
                   :paddingRight 15
                   :height 70
                   :backgroundColor "#009688"
                   :flexDirection "row"
                   :alignItems "center"}
           :elevation 5}
          (touchable-opacity {:onPress #(dispatch! state :change-directory-cancel)}
                             (icon {:name "arrow-back"
                                    :color "white"
                                    :size 30}))
          (view {:flex 1
                 :style {:paddingLeft 15
                         :paddingBottom 10}}
                (text {:style {:fontSize 25
                               :color "white"}}
                      "Change path")
                (text {:style {:color "lightgrey"}}
                      folder))
          (touchable-opacity {:onPress #(dispatch! state :change-directory-accept)}
                             (icon {:name "done"
                                    :color "white"
                                    :size 30})))))
(defc change-directory-loading
  [state]
  (view {:flex 1
         :justifyContent "center"
         :backgroundColor "white"
         :alignItems "center"}
        (text "Loading...")))
(defc change-directory-row-up
  [state]
  (touchable-native-feedback {:onPress #(dispatch! state :change-directory-above)}
                             (view {:padding 15
                                    :flexDirection "row"
                                    :alignItems "center"}
                                   (icon {:name "folder"
                                          :size 20})
                                   (text {:style {:marginLeft 10}}
                                         ".."))))
(defc change-directory-row
  [state x]
  (let [m @state
        item (get (js->clj x :keywordize-keys true) :item)
        file? ((:isFile item))
        color (cond
                (not file?) "grey"
                :else "lightgrey")
        icon-name (if file? "insert-drive-file" "folder")]
    (touchable-native-feedback {:disabled file?
                                :onPress #(dispatch! state :change-directory-folder item)}
                               (view {:padding 15
                                      :flexDirection "row"
                                      :alignItems "center"}
                                     (icon {:name icon-name
                                            :color color
                                            :size 20})
                                     (text {:style {:marginLeft 10
                                                    :color color}}
                                           (:name item))))))
(defc change-directory-main
  [state]
  (let [m @state
        raw-change-directory (get-in m [:change-directory :directory] [])
        folder-loading? (get-in m [:change-directory :loading?] false)
        change-directory-content (->> (get-in m [:change-directory :content] [])
                                      (map #(assoc % :key (:name %)))
                                      (sort (fn [a b]
                                              (cond
                                                (and ((:isFile a)) ((:isDirectory b))) false
                                                (and ((:isDirectory a)) ((:isFile b))) true
                                                :else (compare (:name a) (:name b))))))]
    (if folder-loading?
      (change-directory-loading state)
      (view {:flex 1
             :backgroundColor "white"}
            (flat-list (cond-> {:data change-directory-content
                                :renderItem #(change-directory-row state %)}
                         (> (count raw-change-directory) 1) (assoc :ListHeaderComponent (change-directory-row-up state))))))))

(defc change-directory-modal
  [state]
  (let [m @state
        visible? (some? (get m :change-directory))]
    [(modal {:visible visible?
             :animationType "fade"
             :transparent true
             :onRequestClose #(dispatch! state :change-directory-cancel)}
            (view {:flex 1
                   :backgroundColor "rgba(0,0,0,0.5)"}))
     (modal {:visible visible?
             :animationType "slide"
             :transparent true
             :onRequestClose #(dispatch! state :change-directory-cancel)}
            (change-directory-header state)
            (change-directory-main state))]))
