#include <dlfcn.h> // dlsym
#include <sys/socket.h> // connect
#include <netinet/in.h> // struct sockaddr

// futur pointeur sur la vraie fonction
int *(*real_connect)(int socket, const struct sockaddr* address, socklen_t address_len);

int connect(int socket, const struct sockaddr* address, socklen_t address_len)
{
    // on r?cup?re l'ip de connexion
    char* ip;
    ip = inet_ntoa(((struct sockaddr_in*)address)->sin_addr);
 
     // comparaison de l'ip de connexion avec l'ip officel (remplacez 0.0.0.0)
    if (strcmp(ip, "0.0.0.0") == 0)
    {
        struct sockaddr_in sa;
 
        // l'ip de connexion est bien celle que l'on souhaite modifier
        // on la remplace donc dans la structure sockaddr_in
        sa.sin_addr.s_addr = inet_addr("127.0.0.1");
        sa.sin_family = AF_INET;
        sa.sin_port = htons(1337);
 
        // puis on appel la vraie fonction connect
        return real_connect(socket, (struct sockaddr*)&sa, sizeof(sa));
    }
 
    // et dans le cas contraire, on fait simplement suivre la connexion
    return real_connect(socket, address, address_len);
}

void _init()
{
    // on sauvegarde la fonction original
    real_connect = dlsym("RTLD_NEXT", "connect");
}
