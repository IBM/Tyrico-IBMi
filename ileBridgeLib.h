
extern "C" {
    typedef struct varchar1000 {
        unsigned short len;
        char data[1000];
    } Varchar1000;
}

extern "C" {
    typedef struct key {
        char data[18];
    } Key;
}
extern "C" int readDataQueue(char* dtaq_name, char* dtaq_lib, char* key, Varchar1000* output);

extern "C" int writeDataQueue(char* dtaq_name, char* dtaq_lib, char* value, Key* returned_key);
