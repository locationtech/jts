#!ruby

#Inserts licence headers and @version tags if necessary.
#Updates the @version tags.
#Usage: find . -iname '*.java' | xargs --max-args=1 --verbose test/jts/insert-header.rb

filename = ARGV[0]
version = "1.4"

class AssertionFailure < StandardError
end

class Object
  def assert(bool)
    raise AssertionFailure.new("assertion failure") unless bool
  end
end

class Array
  def insert(inx, obj)
    self[inx, 0] = obj
    self
  end
end

classLineIndex = nil
classCommentStartIndex = nil
classCommentEndIndex = nil

input = File.readlines(filename)
(0..input.size-1).each {|i|    
  if input[i] =~ /interface / and input[i] !~ /\*/
    classLineIndex = i
    break
  end
  if input[i] =~ /class / and input[i] !~ /\*/
    classLineIndex = i
    break
  end
  if input[i] =~ /\/\*\*/
    classCommentStartIndex = i
    classCommentEndIndex = nil
  end
  if input[i] =~ /\*\// and classCommentEndIndex == nil
    classCommentEndIndex = i    
  end
}

assert(classLineIndex != nil)
assert((classCommentStartIndex == nil && classCommentEndIndex == nil) || (classCommentStartIndex != classCommentEndIndex))

if (classCommentStartIndex != nil)
  preClassCommentLines = input[0..classCommentStartIndex-1]
  classCommentLines = input[classCommentStartIndex..classCommentEndIndex]
  classCommentLines.each {|line| line.gsub!(/@version.*/, '@version ' + version.to_s) }
  linesBetweenClassCommentAndClass = input[classCommentEndIndex+1..classLineIndex-1]
  linesBetweenClassCommentAndClass.each {|line| assert(line.strip.empty?)}
else
  preClassCommentLines = input[0..classLineIndex-1]
  classCommentLines = ['
/**
 * @version ' + version + '
 */']
end
classLines = input[classLineIndex..-1]

versionFound = false
classCommentLines.each{|line| versionFound = true if line =~ /@version/ }
if !versionFound
  classCommentLines.insert(classCommentLines.size-1, " *")
  classCommentLines.insert(classCommentLines.size-1, " * @version " + version)
end

output = Array.new

licenceFound = false
preClassCommentLines.each {|line| licenceFound = true if line =~ /This library is distributed in the hope/ }
if !licenceFound
  licence = '
/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
'
  output << licence
end

output.concat(preClassCommentLines)
output.concat(classCommentLines)
output.concat(classLines)

file = File.open(filename, "w")
output.each {|line| file.puts line }
