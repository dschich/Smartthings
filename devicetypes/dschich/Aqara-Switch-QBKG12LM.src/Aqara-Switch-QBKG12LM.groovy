/**
 *  Xiaomi Aqara Switch QBKG12LM
 *  ver 1.0
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Original device handler code by Tomas Axerot for Ubisys Power switch S2, adapted with code parts of 
 *  Aqara model by bspranger, for Xiaomi Aqara Switch QBKG12LM by dschich
 *  
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

metadata {
	//DTH for Xiaomi Aqara Switch QBKG12LM
    definition (name: "Xiaomi Aqara Switch QBKG12LM", namespace: "dschich", author: "Diego Schich") {
        capability "Actuator"
        capability "Switch"
        //capability "Battery"
        //capability "Configuration"
        capability "Refresh"
        capability "Sensor"
        capability "Health Check"
        capability "Light"
        
        attribute "lastCheckin", "string"
        
		
        fingerprint profileId: "0104", deviceId: "0051", inClusters: "0000,0001,0002,0003,0004,0005,0006,0010,000A", outClusters: "0019,000A", manufacturer: "LUMI", model: "lumi.ctrl_ln2.aq1", deviceJoinName: "Aqara Switch QBKG12LM"
                
    }
    
    preferences {
		//Date & Time Config
		input description: "", type: "paragraph", element: "paragraph", title: "DATE & CLOCK"    
		input name: "dateformat", type: "enum", title: "Set Date Format\n US (MDY) - UK (DMY) - Other (YMD)", description: "Date Format", options:["US","UK","Other"]
		input name: "clockformat", type: "bool", title: "Use 24 hour clock?"
        // Temp Adjust
        input "tempOffset", "number", title: "Temperature Offset", description: "Adjust temperature in degrees", range: "*..*", displayDuringSetup: false
		input title: "", description: "Use to correct any temperature variations by selecting an offset.", displayDuringSetup: false, type: "paragraph", element: "paragraph"

	}
    
    
	tiles(scale: 2) {
		multiAttributeTile(name: "switch", type: "device.switch", width: 6, height: 4, canChangeIcon: false) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: 'On', action: "switch.off", icon:"st.switches.switch.on", backgroundColor: "#00A0DC", nextState: "turningOff"
				attributeState "off", label: 'Off', action: "switch.on", icon:"st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
				attributeState "turningOn", label: 'Turning On', action: "switch.off", icon:"st.switches.switch.on", backgroundColor: "#00A0DC", nextState: "turningOff"
				attributeState "turningOff", label: 'Turning Off', action: "switch.on", icon:"st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
			}
            tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") {
    			attributeState("default", label:'Last Update: ${currentValue}')
		   	}

		}
        valueTile("deviceTemperature", "device.deviceTemperature", inactiveLabel: false, width: 2, height: 2) {
            state "deviceTemperature", label:'${currentValue}°C', icon:"st.Weather.weather2"

        }
        valueTile("spacer", "spacer", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
	    state "default", label:''
        }
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
		}
        standardTile("voltage", "device.voltage", inactiveLabel: false, decoration:"flat", width: 2, height: 2) {
            state "voltage", label:'${currentValue} Volts', icon:"https://raw.githubusercontent.com/bspranger/Xiaomi/master/images/XiaomiPressure.png"
        }

		main "switch"
		details(["switch", "refresh", "deviceTemperature", "voltage"])
	}   

}

def parse(String description) {
    log.debug "Log descrição: $description"
    //def event = zigbee.getEvent(description)
    if (event) {
        sendEvent(event)
    }
    else {
        if ((description?.startsWith("catchall:")) || (description?.startsWith("read attr -"))) {
            def descMap = zigbee.parseDescriptionAsMap(description)
            //log.warn "$descMap"
            //lastCheckin
            if (descMap.clusterInt == 0x0006 && descMap.attrInt == 0x00 && descMap.commandInt == 0x01) {
                if(descMap.sourceEndpoint == "01") {
                 def now = formatDate()
                 sendEvent(name: "lastCheckin", value: now)
                 return createEvent(name: "switch", value: descMap.value == "01" ? "on" : "off")
              } else if(descMap.sourceEndpoint == "02") {              
                 return childDevices[0].sendEvent(name: "switch", value: descMap.value == "01" ? "on" : "off")
              }
            }
            // APP Button
            else if (descMap.clusterInt == 0x0006 && descMap.commandInt == 0x0B) {
                if(descMap.sourceEndpoint == "01") {               
                 return createEvent(name: "switch", value: descMap.data[0] == "01" ? "on" : "off")
              } else if(descMap.sourceEndpoint == "02") {              
                return childDevices[0].sendEvent(name: "switch", value: descMap.data[0] == "01" ? "on" : "off")
              }
            }
            // Switch Button
            else if (descMap.clusterInt == 0x0006 && descMap.attrInt == 0x00) {
                def cmds = zigbee.readAttribute(0x0006, 0x0000, [destEndpoint: 0x01]) + zigbee.readAttribute(0x0006, 0x0000, [destEndpoint: 0x02])     
                 return cmds.collect { new physicalgraph.device.HubAction(it) }
                if(descMap.endpoint == "01") {             
                 return createEvent(name: "switch", value: descMap.value[0] == "01" ? "on" : "off")
             } else if(descMap.endpoint == "02") {             
                return childDevices[0].sendEvent(name: "switch", value: descMap.value[0] == "01" ? "on" : "off")
             }
            }
            // Voltage
            else if (descMap.clusterInt == 0x0001 && descMap.attrInt == 0x000) {
                def result = descMap.value[0..3]
                float voltage = Integer.parseInt(result, 16) /10
                //log.debug voltage
                sendEvent(name: "voltage", value: voltage, displayed: true)
            }
            // Temperature
            else if (descMap.clusterInt == 0x0002 && descMap.attrInt == 0x000) {
                 def temp = descMap.value[0..3]
                 float deviceTemperature = Integer.parseInt(temp)
                 if (tempOffset) {
                  deviceTemperature = (int) deviceTemperature + (int) tempOffset
                 }
                 log.debug "$deviceTemperature °C"
                 sendEvent(name: "deviceTemperature", value: deviceTemperature, displayed: false)
            }
        }
 
        //log.debug "Parse returned $event"
        def result = event ? createEvent(event) : []
 
    }
}

def off() {
	log.trace "off"
    zigbee.off()
}

def on() {
	log.trace "on"
    zigbee.on()
}

void off2() {	
	log.trace "off2"        
    
    def actions = [new physicalgraph.device.HubAction("st cmd 0x${device.deviceNetworkId} 0x02 0x0006 0x00 {}")]    
    sendHubCommand(actions)
}

void on2() {	
	log.trace "on2"
    
    def actions = [new physicalgraph.device.HubAction("st cmd 0x${device.deviceNetworkId} 0x02 0x0006 0x01 {}")]    
    sendHubCommand(actions)    
}

def installed() {
	log.trace "installed"
    createChildDevices()
    checkIntervalEvent("installed")
}

private void createChildDevices() {
    log.trace "createChildDevices"
    
    // Save the device label for updates by updated()
    state.oldLabel = device.label
    
    addChildDevice("dschich", "Aqara Switch QBKG12LM - Button 2", "${device.deviceNetworkId}-2", null,[componentLabel: "${device.displayName} (B2)", isComponent: false, componentName: "b2"])
}

def ping() {
	log.trace "ping"
	return zigbee.onOffRefresh()
}

def refresh() {
	// log.debug "refresh"    
	
def refreshCmds = zigbee.readAttribute(0x0001, 0x0000, [destEndpoint: 0x01]) +
				  zigbee.readAttribute(0x0002, 0x0000, [destEndpoint: 0x01]) +
                  zigbee.readAttribute(0x0006, 0x0000, [destEndpoint: 0x01]) +
    			  zigbee.readAttribute(0x0006, 0x0000, [destEndpoint: 0x02])                   
                
    return refreshCmds
}


private checkIntervalEvent(text) {
    // Device wakes up every 1 hours, this interval allows us to miss one wakeup notification before marking offline
    log.debug "${device.displayName}: Configured health checkInterval when ${text}()"
    sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
}

def formatDate(batteryReset) {
    def correctedTimezone = ""
    def timeString = clockformat ? "HH:mm:ss" : "h:mm:ss aa"

	// If user's hub timezone is not set, display error messages in log and events log, and set timezone to GMT to avoid errors
    if (!(location.timeZone)) {
        correctedTimezone = TimeZone.getTimeZone("GMT")
        log.error "${device.displayName}: Time Zone not set, so GMT was used. Please set up your location in the SmartThings mobile app."
        sendEvent(name: "error", value: "", descriptionText: "ERROR: Time Zone not set, so GMT was used. Please set up your location in the SmartThings mobile app.")
    } 
    else {
        correctedTimezone = location.timeZone
    }
    if (dateformat == "US" || dateformat == "" || dateformat == null) {
        if (batteryReset)
            return new Date().format("MMM dd yyyy", correctedTimezone)
        else
            return new Date().format("EEE MMM dd yyyy ${timeString}", correctedTimezone)
    }
    else if (dateformat == "UK") {
        if (batteryReset)
            return new Date().format("dd MMM yyyy", correctedTimezone)
        else
            return new Date().format("EEE dd MMM yyyy ${timeString}", correctedTimezone)
        }
    else {
        if (batteryReset)
            return new Date().format("yyyy MMM dd", correctedTimezone)
        else
            return new Date().format("EEE yyyy MMM dd ${timeString}", correctedTimezone)
    }
}
