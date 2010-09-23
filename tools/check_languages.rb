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
items         = {}
pattern       = Regexp.new '<string name="([^"]+)">([^<]+)</string>'
lang_pattern  = Regexp.new 'values-([a-zA-Z_-]+)'
show_keys     = false

show_keys = true if ARGV.length == 1 && ARGV[0] == '--show-keys'

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
    items[result[1]] = result[2]
  end
end
file.close

puts "Found #{items.length} items in strings.xml"
puts

# Check all langauges files for keys
languages.each { |language|
  keys          = 0
  check         = items.clone
  untranslated  = {}
  unused        = {}
  language_file = "#{base_path}values-#{language}/strings.xml"

  file = File.new(language_file, 'r')
  while line = file.gets
    result = pattern.match line
    if !result.nil? then
      key = result[1]
      value = result[2]

      check.delete key
      if items[key].nil? then
        unused[key] = value
      else
        untranslated[key] = value if items[key] == value
      end
      keys += 1
    end
  end

  translated = items.length - untranslated.length - check.length
  translated_percent = sprintf('%.2f', 100.to_f / items.length.to_f * translated.to_f)
  keys_percent = sprintf('%.2f', 100.to_f / items.length.to_f * keys.to_f)

  puts "Language #{language}"
  puts " * Keys:       #{keys.to_s.rjust 5}/#{items.length.to_s.ljust 5} #{keys_percent.to_s.rjust 6}%"
  puts " * Translated: #{translated.to_s.rjust 5}/#{items.length.to_s.ljust 5} #{translated_percent.to_s.rjust 6}%"

  if show_keys then
    if check.length > 0 then
      puts " * Missing keys:"
      check.each { |key,value| puts "   * #{key}" }
    end

    if untranslated.length > 0 then
      puts " * Untranslated keys:"
      untranslated.each { |key,value| puts "   * #{key}" }
    end

    if unused.length > 0 then
      puts " * Unused keys:"
      unused.each { |key,value| puts "   * #{key}" } if unused.length > 0
    end
  end

  puts
}

