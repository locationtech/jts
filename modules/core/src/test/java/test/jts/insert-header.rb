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
classLines = input[classLineIndex..-1]                  OR

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
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
'
  output << licence
end

output.concat(preClassCommentLines)
output.concat(classCommentLines)
output.concat(classLines)

file = File.open(filename, "w")
output.each {|line| file.puts line }
