metadata {
	definition (name: "Aqara Switch QBKG12LM - Button 2", namespace: "dschich", author: "Diego Schich") {
        capability "Switch"
        capability "Actuator"
        capability "Sensor"
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "switch", type: "device.switch", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: 'On', action: "switch.off", icon:"st.switches.switch.on", backgroundColor: "#00A0DC", nextState: "turningOff"
				attributeState "off", label: 'Off', action: "switch.on", icon:"st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
				attributeState "turningOn", label: 'Turning On', action: "switch.off", icon:"st.switches.switch.on", backgroundColor: "#00A0DC", nextState: "turningOff"
				attributeState "turningOff", label: 'Turning Off', action: "switch.on", icon:"st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
			}
            

		main "switch"
		details(["switch"])
		}
	}
}
def on() {
	parent.on2()
}

def off() {
	parent.off2()
}
