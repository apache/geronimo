import org.apache.geronimo.kernel.repository.ImportType

assert null != configurationData : 'property configurationData is not exported'
assert null != configurationDataBuilder : 'property configurationDataBuilder is not exported'

configurationDataBuilder.configure {
    addDependency(groupId: "group", artifactId: "artifactToAdd", version: "1.0", type: "jar", importType: ImportType.SERVICES)
    removeDependency(groupId: "group", artifactId: "artifactToRemove", version: "1.0")
}
