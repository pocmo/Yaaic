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

# TODO: Use a XML parser instead of reading lines

base_path     = "#{File.dirname(__FILE__)}/../res/"
original_file = "#{base_path}values/strings.xml"
languages     = []
items         = []
pattern       = Regexp.new '<string name="([^"]+)">([^<]+)</string>'
lang_pattern  = Regexp.new 'values-([a-zA-Z_-]+)'

# Scan for languages
Dir.new(base_path).entries.each { |directory|
  result = lang_pattern.match directory
  if !result.nil? then
    languages.push result[1]
  end
}

puts "Found #{languages.length} language(s): #{languages.inspect}"

# Grab all keys from the original file
file = File.new(original_file, 'r')
while line = file.gets
  result = pattern.match line
  if !result.nil? then
    items.push result[1]
  end
end
file.close

puts "Found #{items.length} items in strings.xml"
puts

# Check all langauges files for keys
languages.each { |language| 
  check = items.clone
  language_file = "#{base_path}values-#{language}/strings.xml"

  file = File.new(language_file, 'r')
  while line = file.gets
    result = pattern.match line
    if !result.nil? then
      check.delete result[1]
    end
  end

  percent = sprintf('%.2f', 100 - (100.to_f / items.length.to_f * check.length.to_f))

  if check.length == 0 then
    puts "Language #{language} is OK (Translated: #{percent}%)"
  else
    puts "Language #{language} has missing translations (Translated: #{percent}%)"
    check.each { |key| puts "  #{key}" }
  end

  puts
}

