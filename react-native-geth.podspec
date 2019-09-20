require "json"
package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
    s.name         = "react-native-geth"
    s.version      = package['version']
    s.summary      = package['description']

    s.license      = package['license']
    s.authors      = package['author']
    s.homepage     = package['homepage']
    s.platform     = :ios, "9.0"

    s.source       = { :git => package["repository"]["url"], :tag => s.version }
    s.source_files  = "ios/*.{h,m,swift}"
    s.requires_arc = true

    s.dependency 'CeloBlockchain'
    s.dependency 'React'

    s.libraries = 'bls_zexe'
    s.xcconfig = { 'LIBRARY_SEARCH_PATHS' => '${PODS_ROOT}/../../../../node_modules/@celo/client/vendor/github.com/celo-org/bls-zexe/bls/target/universal/release' }
end
