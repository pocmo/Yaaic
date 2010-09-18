#!/usr/bin/ruby
# Yaaic - Yet Another Android IRC Client
#
# Copyright 2009-2010 Sebastian Kaspari
#
# This file is part of Yaaic.
#
# Yaaic is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Yaaic is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with Yaaic.  If not, see <http://www.gnu.org/licenses/>.

# TODO: Maybe check all existing languages instead of using a paremter
# TODO: Add a basepath to the files (this script should be callable from
#       everyhwere
# TODO: Use a XML parser instead of reading lines

if ARGV.length != 1 then
  puts "Which language should be checked...?"
  exit
end

language = ARGV[0]

original_file = "../res/values/strings.xml"
language_file = "../res/values-#{language}/strings.xml"

if !File.exists? language_file then
  puts "File does not exists: #{language_file}"
  exit
end

# Grab all keys from the original file
items = []

pattern = Regexp.new '<string name="([^"]+)">([^<]+)</string>'

file = File.new(original_file, 'r')
while line = file.gets
  result = pattern.match line
  if !result.nil? then
    items.push result[1]
  end
end
file.close

puts "Found #{items.length} items in strings.xml"

puts "Checking #{language}"
check = items.clone

file = File.new(language_file, 'r')
while line = file.gets
  result = pattern.match line
  if !result.nil? then
    check.delete result[1]
  end
end

percent = 100 - (100.to_f / items.length.to_f * check.length.to_f)

if check.length == 0 then
  puts "Language #{language} is OK (Translated: #{percent}%)"
else
  puts "Language #{language} has missing translations (Translated: #{percent}%)"
  check.each { |key| puts "  #{key}" }
end

