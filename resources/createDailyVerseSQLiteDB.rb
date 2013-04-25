#!/usr/bin/ruby

require 'sqlite3'

db = SQLite3::Database.new("dailyVerse.sqlite")

verseTable = "DailyVerse"

# Android Metadata relation
db.execute("CREATE TABLE 'android_metadata' ('locale' TEXT DEFAULT 'en_US');")
db.execute("INSERT INTO 'android_metadata' VALUES ('en_US')")

# _id is for android id purposes
db.execute("create table '#{verseTable}' ('_id' INTEGER PRIMARY KEY, 'referenceNumber' INTEGER, 'dateLastSeen' TEXT);")

366.times do | i |
    randomVerse = rand(31100) + 1
    db.execute("INSERT INTO '#{verseTable}' VALUES (#{i}, #{randomVerse}, NULL)")

end

