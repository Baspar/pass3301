(ns pass-android.android.core
  (:require-macros [rum.core :refer [defc]])
  (:require [re-natal.support :as support]
            [pass-android.android.password-list :refer [password-list-page]]
            [pass-android.android.modals.decrypt-password :refer [password-modal]]
            [pass-android.android.modals.change-directory :refer [change-directory-modal]]
            [pass-android.android.modals.drawer :refer [drawer-modal]]
            [pass-android.android.utils :refer [create-element hex-alph]]
            [pass-android.android.state :refer [app-state refs]]
            [pass-android.android.actions :refer [dispatch!]]
            [rum.core :as rum]))

(set! js/window.React (js/require "react"))
(def ReactNative (js/require "react-native"))
(def fs (js/require "react-native-fs"))

(def status-bar (partial create-element (.-StatusBar ReactNative)))
(def view (partial create-element (.-View ReactNative)))

(def app-registry (.-AppRegistry ReactNative))

(defc AppRoot [state]
  (view {:flex 1}
        (drawer-modal state
                      (status-bar {:backgroundColor "#009688"})

                      (view {:style {:position "absolute"}}
                            (password-modal state))
                      (view {:style {:position "absolute"}}
                            (change-directory-modal state))
                      (password-list-page state))))

(defonce root-component-factory (support/make-root-component-factory))

(defn mount-app [] (support/mount (AppRoot app-state)))

;; Render watch
(remove-watch app-state :app-state)
(add-watch app-state :app-state #(mount-app))

;; Modal opener watcher
(remove-watch app-state :drawer)
(add-watch app-state :drawer
           (fn [_ _ old-s new-s]
             (let [drawer-ref (get @refs :drawer)
                   old-drawer-open? (get old-s :drawer-open? false)
                   new-drawer-open? (get new-s :drawer-open? false)]
               (cond
                 (nil? drawer-ref) nil
                 (and old-drawer-open? (not new-drawer-open?)) (.closeDrawer drawer-ref)
                 (and (not old-drawer-open?) new-drawer-open?) (.openDrawer drawer-ref)))))

(defn init []
  (mount-app)
  (dispatch! app-state :refresh-files)
  (.registerComponent app-registry "passAndroid" (fn [] root-component-factory)))
