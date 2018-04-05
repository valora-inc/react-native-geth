Pod::Spec.new do |s|
    s.name         = "ReactNativeGeth"
    s.version      = "1.0.0"
    s.summary      = "ReactNativeGeth"
    s.license      = "MIT"
    s.platform     = :ios, "9.0"
    s.source_files  = "ios/*.{h,m}"
    s.requires_arc = true
    s.dependency 'Geth'
end
