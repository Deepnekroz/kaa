package org.kaaproject.kaa.server.control.service.modularization;

import org.apache.avro.Schema;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaInfoDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto;
import org.kaaproject.kaa.common.dto.plugin.ContractDto;
import org.kaaproject.kaa.common.dto.plugin.ContractItemDto;
import org.kaaproject.kaa.common.dto.plugin.PluginContractDto;
import org.kaaproject.kaa.common.dto.plugin.PluginContractInstanceDto;
import org.kaaproject.kaa.common.dto.plugin.PluginContractInstanceItemDto;
import org.kaaproject.kaa.common.dto.plugin.PluginContractItemDto;
import org.kaaproject.kaa.common.dto.plugin.PluginDto;
import org.kaaproject.kaa.common.dto.plugin.PluginInstanceDto;
import org.kaaproject.kaa.common.dto.plugin.PluginInstanceState;
import org.kaaproject.kaa.server.plugin.messaging.gen.ItemConfiguration;
import org.kaaproject.kaa.server.plugin.messaging.gen.test.ClassA;
import org.kaaproject.kaa.server.plugin.messaging.gen.test.ClassB;
import org.kaaproject.kaa.server.plugin.messaging.gen.test.ClassC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class HardCodedPluginInstanceFactory {

    private static final Logger LOG = LoggerFactory.getLogger(HardCodedPluginInstanceFactory.class);

    private static final GenericAvroConverter<ItemConfiguration>
            METHOD_NAME_CONVERTER = new GenericAvroConverter<>(ItemConfiguration.SCHEMA$);

    public static enum Type {
        MESSAGING, REST
    }

    public static PluginInstanceDto create(Type type, PluginDto pluginDto) {
        switch (type) {
            case MESSAGING:
                return createEndpointMessagingInstance(pluginDto);
            case REST:
            default:
                return null;
        }
    }

    private static PluginInstanceDto createEndpointMessagingInstance(PluginDto pluginDto) {
        PluginInstanceDto pluginInstanceDto = new PluginInstanceDto();
        pluginInstanceDto.setPluginDefinition(pluginDto);
        pluginInstanceDto.setName("Endpoint messaging plugin instance");
        pluginInstanceDto.setState(PluginInstanceState.ACTIVE);
        pluginInstanceDto.setConfigurationData("Configuration data for endpoint messaging plugin instance");

        Set<PluginContractInstanceDto> pluginContractInstances = new HashSet<>();
        PluginContractInstanceDto pluginContractInstanceDto = new PluginContractInstanceDto();
        pluginContractInstances.add(pluginContractInstanceDto);
        pluginInstanceDto.setContracts(pluginContractInstances);

        PluginContractDto endpointMessagingPluginContract = getPluginContractForContractName("Messaging SDK contract", pluginDto);
        addItemsToPluginContractInstance(pluginContractInstanceDto, endpointMessagingPluginContract);
        pluginContractInstanceDto.setContract(endpointMessagingPluginContract);

        return pluginInstanceDto;
    }

    private static PluginContractDto getPluginContractForContractName(String name, PluginDto pluginDto) {
        Set<PluginContractDto> pluginContractDtos = pluginDto.getPluginContracts();
        for (PluginContractDto pluginContractDto : pluginContractDtos) {
            if (name.equals(pluginContractDto.getContract().getName())) {
                return pluginContractDto;
            }
        }
        return null;
    }

    private static void addItemsToPluginContractInstance(PluginContractInstanceDto pluginContractInstanceDto,
                                                         PluginContractDto pluginContract) {
        Set<PluginContractInstanceItemDto> items = new HashSet<>();

        ContractDto contract = pluginContract.getContract();
        PluginContractItemDto sendMessagePluginContractItem = getPluginContractItemByName(pluginContract, "sendMessage");
        items.add(getPluginContractInstanceItem("sendA", ClassA.SCHEMA$, null, sendMessagePluginContractItem, contract));
        items.add(getPluginContractInstanceItem("getA", null, ClassA.SCHEMA$, sendMessagePluginContractItem, contract));
        items.add(getPluginContractInstanceItem("getB", ClassA.SCHEMA$, ClassB.SCHEMA$, sendMessagePluginContractItem, contract));
        items.add(getPluginContractInstanceItem("getC", ClassA.SCHEMA$, ClassC.SCHEMA$, sendMessagePluginContractItem, contract));

        PluginContractItemDto receiveMessagePluginContractItem = getPluginContractItemByName(pluginContract, "receiveMessage");
        items.add(getPluginContractInstanceItem("setMethodAListener", ClassC.SCHEMA$, ClassA.SCHEMA$, receiveMessagePluginContractItem, contract));
        items.add(getPluginContractInstanceItem("setMethodBListener", ClassC.SCHEMA$, ClassB.SCHEMA$, receiveMessagePluginContractItem, contract));
        items.add(getPluginContractInstanceItem("setMethodCListener", null, ClassC.SCHEMA$, receiveMessagePluginContractItem, contract));

        pluginContractInstanceDto.setItems(items);
    }

    private static PluginContractItemDto getPluginContractItemByName(PluginContractDto pluginContract, String name) {
        for (PluginContractItemDto pluginContractItem : pluginContract.getPluginContractItems()) {
            ContractItemDto contractItem = pluginContractItem.getContractItem();
            if (contractItem.getName().equals(name)) {
                return pluginContractItem;
            }
        }
        return null;
    }

    private static PluginContractInstanceItemDto getPluginContractInstanceItem(String methodName, Schema inSchema, Schema outSchema,
                                                                               PluginContractItemDto pluginContractItem, ContractDto contract) {
        PluginContractInstanceItemDto instanceItemDto = new PluginContractInstanceItemDto();
        try {
            String confData = METHOD_NAME_CONVERTER.encodeToJson(new ItemConfiguration(methodName));
            instanceItemDto.setConfData(confData);
            instanceItemDto.setInMessageSchema(getSchema(inSchema, contract));
            instanceItemDto.setOutMessageSchema(getSchema(outSchema, contract));
            instanceItemDto.setPluginContractItem(pluginContractItem);
        } catch (IOException e) {
            LOG.error("Can't encode to json", e);
        }
        return instanceItemDto;
    }

    private static CTLSchemaDto getSchema(Schema schema, ContractDto contract) {
        if (schema == null) {
            return null;
        }
        CTLSchemaInfoDto ctlSchemaInfo = new CTLSchemaInfoDto();
        ctlSchemaInfo.setFqn(schema.getFullName());
        // CTL schema version is taken from the corresponding contract
        ctlSchemaInfo.setVersion(contract.getVersion());
        ctlSchemaInfo.setScope(CTLSchemaScopeDto.SYSTEM);
        return new CTLSchemaDto(ctlSchemaInfo, null);
    }
}