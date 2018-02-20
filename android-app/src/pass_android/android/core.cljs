(ns pass-android.android.core
  (:require-macros [rum.core :refer [defc]])
  (:require [re-natal.support :as support]
            [pass-android.android.password-list :refer [password-list-page]]
            [pass-android.android.modals :refer [password-modal]]
            [pass-android.android.utils :refer [create-element hex-alph]]
            [rum.core :as rum]))

(set! js/window.React (js/require "react"))
(def ReactNative (js/require "react-native"))
(def fs (js/require "react-native-fs"))

(def status-bar (partial create-element (.-StatusBar ReactNative)))
(def view (partial create-element (.-View ReactNative)))

(def app-registry (.-AppRegistry ReactNative))

(defonce app-state
  (atom {:folder (str (.-ExternalStorageDirectoryPath fs) "/.pass")
         :files []}))

(defc AppRoot [state]
  (view {:flex 1}
        (status-bar {:backgroundColor "#009688"})
        (view {:style {:position "absolute"}}
              (password-modal state))
        (password-list-page state)))

(defonce root-component-factory (support/make-root-component-factory))

(defn mount-app [] (support/mount (AppRoot app-state)))

(remove-watch app-state :app-state)
(add-watch app-state
           :app-state
           #(mount-app))

(defn init []
      (mount-app)
      (.registerComponent app-registry "passAndroid" (fn [] root-component-factory)))
