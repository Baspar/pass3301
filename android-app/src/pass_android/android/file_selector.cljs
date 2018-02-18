(ns pass-android.android.file-selector
  (:require-macros [rum.core :refer [defc]])
  (:require [re-natal.support :as support]
            [pass-android.android.utils :refer [create-element]]
            [rum.core :as rum]))


(set! js/window.React (js/require "react"))
(def ReactNative (js/require "react-native"))
(def fs (js/require "react-native-fs"))
