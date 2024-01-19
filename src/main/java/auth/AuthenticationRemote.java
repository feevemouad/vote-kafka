package auth;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AuthenticationRemote extends Remote {
    boolean authenticate(String username, String password) throws RemoteException;
    boolean register(String Username, String Password, boolean IsAdmin) throws RemoteException;
    boolean isAdmin(String username) throws RemoteException;
}