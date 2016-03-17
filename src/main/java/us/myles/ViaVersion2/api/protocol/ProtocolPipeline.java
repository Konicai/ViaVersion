package us.myles.ViaVersion2.api.protocol;

import us.myles.ViaVersion.packets.Direction;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion2.api.PacketWrapper;
import us.myles.ViaVersion2.api.data.UserConnection;
import us.myles.ViaVersion2.api.protocol.base.BaseProtocol;
import us.myles.ViaVersion2.api.protocol.base.ProtocolInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ProtocolPipeline extends Protocol {
    LinkedList<Protocol> protocolList;
    private UserConnection userConnection;

    public ProtocolPipeline(UserConnection userConnection) {
        super();
        init(userConnection);
    }

    @Override
    protected void registerPackets() {
        protocolList = new LinkedList<>();
        // This is a pipeline so we register basic pipes
        protocolList.addLast(new BaseProtocol());
    }

    @Override
    public void init(UserConnection userConnection) {
        this.userConnection = userConnection;

        ProtocolInfo protocolInfo = new ProtocolInfo(userConnection);
        protocolInfo.setPipeline(this);

        userConnection.put(protocolInfo);

        /* Init through all our pipes */
        for (Protocol protocol : protocolList) {
            protocol.init(userConnection);
        }
    }

    public void add(Protocol protocol) {
        if (protocolList != null) {
            System.out.println("Adding protocol to list!!");
            protocolList.addLast(protocol);
            protocol.init(userConnection);
        } else {
            throw new NullPointerException("Tried to add protocol to early");
        }
    }

    @Override
    public void transform(Direction direction, State state, PacketWrapper packetWrapper) throws Exception {
//        System.out.println("--> Packet ID incoming: " + packetWrapper.getId() + " - " + state);
        List<Protocol> protocols = new ArrayList<>(protocolList);
        // Other way if outgoing
        if (direction == Direction.OUTGOING)
            Collections.reverse(protocols);

        for (Protocol protocol : protocols) { // Copy to prevent from removal.
            System.out.println("Calling " + protocol.getClass().getSimpleName() + " " + direction);
            protocol.transform(direction, state, packetWrapper);
            // Reset the reader for the packetWrapper (So it can be recycled across packets)
            packetWrapper.resetReader();
        }
        super.transform(direction, state, packetWrapper);
        if (packetWrapper.getId() != 37 && packetWrapper.getId() != 32 && packetWrapper.getId() != 52 && packetWrapper.getId() != 21 && packetWrapper.getId() != 59)
            System.out.println("--> Sending Packet ID: " + packetWrapper.getId() + " " + state + " " + direction);
    }

    public boolean contains(Class<? extends Protocol> pipeClass) {
        for (Protocol protocol : protocolList) {
            if (protocol.getClass().equals(pipeClass)) return true;
        }
        return false;
    }
}