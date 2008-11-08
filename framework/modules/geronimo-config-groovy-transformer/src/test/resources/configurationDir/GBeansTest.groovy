import org.apache.geronimo.kernel.config.transformer.DummyGBean

assert null != configurationData : 'property configurationData is not exported'
assert null != gbeanDatas : 'property gbeanDatas is not exported'
assert null != gbeanDataBuilder : 'property gbeanDataBuilder is not exported'

gbeanDataBuilder.configure {
    addGBean(name: 'name', gbean: DummyGBean) {
        attribute(attributeName: 'value')
        reference('referenceName') {
            pattern('group/artifact//?name=name')
            pattern('group/artifact//?name=name2')
        }
    }
}
