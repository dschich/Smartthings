/**
 *  Device Handler for Aqara Switch QBKG12LM - Button 2
 *  ver 1.0
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 
 *  Original device handler code by Tomas Axerot for Ubisys Power switch S2, adapted for Xiaomi Aqara Switch QBKG12LM by dschich
 *  
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

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
