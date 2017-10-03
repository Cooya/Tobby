//
//  main.cpp
//  injection
//
//  Created by Alexandre Martin on 14/07/2014.
//  Copyright (c) 2014 scalexm. All rights reserved.
//

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <iostream>
#include <dlfcn.h>


int (*real_connect) (int, const struct sockaddr *, socklen_t) = NULL;

int connect(int sockfd, const struct sockaddr * addr, socklen_t addrlen)
{
    if (!real_connect)
        real_connect = reinterpret_cast<decltype(real_connect)>(dlsym(RTLD_NEXT, "connect"));

    auto in = reinterpret_cast<const sockaddr_in *>(addr);
    if (ntohs(in->sin_port) == 443 || ntohs(in->sin_port) == 5555)
    {
        struct sockaddr_in serv_addr;
        struct hostent * server = gethostbyname("127.0.0.1");
        bzero((char *) &serv_addr, sizeof(serv_addr));
        serv_addr.sin_family = AF_INET;
        bcopy((char *)server->h_addr,
              (char *)&serv_addr.sin_addr.s_addr,
              server->h_length);
        if (ntohs(in->sin_port) == 443)
            serv_addr.sin_port = htons(2000);
        if (ntohs(in->sin_port) == 5555)
            serv_addr.sin_port = htons(2001);
        return real_connect(sockfd,(struct sockaddr *) &serv_addr, sizeof(serv_addr));
    }
    else
        return real_connect(sockfd, addr, addrlen);
}