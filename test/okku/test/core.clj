(ns okku.test.core
  (:use [okku.core])
  (:use [clojure.test]))

(deftest test-!
  (are [x y] (= (macroexpand-1 (quote x)) y)
       (okku.core/! msg) '(.tell (.getSender this) msg (.getSelf this))
       (okku.core/! target msg) '(.tell target msg (.getSelf this))))

(deftest test-spawn
  (are [x y] (= (macroexpand-1 (quote x)) y)
       (okku.core/spawn act) '(.actorOf (.getContext this) act)
       (okku.core/spawn act :in asys :router router :name name)
       '(.actorOf asys (okku.core/with-router act router) name)
       (okku.core/spawn act :deploy-on addr)
       '(.actorOf
          (.getContext this)
          (okku.core/with-deploy act addr))))

(deftest test-dispatch-on
  (are [x y] (= (macroexpand-1 x) y)
       '(okku.core/dispatch-on t
                               :dv1 (answer1)
                               :dv2 (answer2))
       '(clojure.core/cond (clojure.core/= t :dv1) (answer1)
              (clojure.core/= t :dv2) (answer2)
              :else (.unhandled this t))))

(deftest test-string-to-vec
  (are [x y] (= x (@#'okku.core/string-to-vec y))
       ["akka" "sys" "hostname" "port" ["path1" "path2"]]
       "akka://sys@hostname:port/path1/path2"
       ["akka" "CalculatorApplication" "127.0.0.1" "2552" ["user" "simpleCalculator"]]
       "akka://CalculatorApplication@127.0.0.1:2552/user/simpleCalculator"))

(deftest test-vec-to-string
  (are [x y] (= y (@#'okku.core/vec-to-string x))
       ["akka" "sys" "hostname" "port" ["path1" "path2"]]
       "akka://sys@hostname:port/path1/path2"
       ["akka" "CalculatorApplication" "127.0.0.1" "2552" ["user" "simpleCalculator"]]
       "akka://CalculatorApplication@127.0.0.1:2552/user/simpleCalculator"))

(deftest test-get-config-lookup
  (are [x y] (= x (@#'okku.core/get-config-lookup
                       (..
                         (com.typesafe.config.ConfigFactory/parseString y)
                         root)
                       "name"))
       ["akka" "sys" "hostname" "port" ["path1" "path2"]]
       "okku.lookup./name {
       protocol = akka
       actor-system = sys
       hostname = hostname
       port = port
       path = /path1/path2
       }
       "
       ["akka" nil nil "port" ["user" "path1" "path2"]]
       "okku.lookup./name {
       protocol = akka
       port = port
       path = path1/path2
       }
       "
       [nil nil nil nil nil] ""))

(deftest test-merge-addresses
  (are [x y z] (= x (@#'okku.core/merge-addresses y z))
       ["akka" "sys" "hn" "port" ["path1" "path2"]]
       ["akka" nil nil "port" nil]
       ["other" "sys" "hn" "other" ["path1" "path2"]]))
