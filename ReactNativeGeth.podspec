require "json"
package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
    s.name         = "ReactNativeGeth"
    s.version      = package['version']
    s.summary      = package['description']

    s.license      = package['license']
    s.authors      = package['author']
    s.homepage     = package['homepage']
    s.platform     = :ios, "9.0"

    s.source       = { :git => "https://github.com/YsnKsy/react-native-geth" }
    s.source_files  = "ios/*.{h,m,swift}"
    s.requires_arc = true

    s.dependency 'Geth'
    s.dependency 'React'
end
