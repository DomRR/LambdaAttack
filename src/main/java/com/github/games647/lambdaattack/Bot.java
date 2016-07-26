package com.github.games647.lambdaattack;

import java.net.Proxy;
import java.util.logging.Logger;

import org.spacehq.mc.auth.exception.request.RequestException;
import org.spacehq.mc.protocol.MinecraftProtocol;
import org.spacehq.mc.protocol.packet.ingame.client.ClientChatPacket;
import org.spacehq.packetlib.Client;
import org.spacehq.packetlib.Session;
import org.spacehq.packetlib.tcp.TcpSessionFactory;

public class Bot implements AutoCloseable {

    public static char COMMAND_IDENTIFIER = '/';

    private final Proxy proxy;

    private final Logger logger;

    private final String username;
    private final String password;

    private Session session;
    private EntitiyLocation location;

    public Bot(String username, String password) {
        this(username, password, Proxy.NO_PROXY);
    }

    public Bot(String username, String password, Proxy proxy) {
        this.username = username;
        this.password = password;

        this.proxy = proxy;
        this.logger = Logger.getLogger(username);
        this.logger.setParent(LambdaAttack.getLogger());
    }

    public Bot(String username) {
        this(username, "");
    }

    public Bot(String username, Proxy proxy) {
        this(username, "", proxy);
    }

    public MinecraftProtocol authenticate() throws RequestException {
        MinecraftProtocol protocol;
        if (!password.isEmpty()) {
            protocol = new MinecraftProtocol(username, password);
            logger.info("Successfully authenticated user");
        } else {
            protocol = new MinecraftProtocol(username);
        }

        return protocol;
    }

    public boolean isOnline() {
        return session != null && session.isConnected();
    }

    public Session getSession() {
        return session;
    }

    public void connect(String host, int port) throws RequestException {
        MinecraftProtocol account = authenticate();

        Client client = new Client(host, port, account, new TcpSessionFactory(proxy));
        this.session = client.getSession();
        client.getSession().addListener(new SessionListener(this));

        client.getSession().connect();
    }

    public void sendMessage(String message) {
        if (session != null) {
            ClientChatPacket chatPacket = new ClientChatPacket(message);
            session.send(chatPacket);
        }
    }

    public EntitiyLocation getLocation() {
        return location;
    }

    protected void setLocation(EntitiyLocation location) {
        this.location = location;
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    public void close() {
        if (session != null) {
            session.disconnect("Disconnect");
        }
    }
}
