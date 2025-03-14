**free
ctl-opt BNDDIR('TYRICO/BNDDIR');

dcl-pr writeDataQueue extproc('writeDataQueue');
        dtaq_name char(10) const;
        dtaq_lib  char(10) const;
        value pointer value options(*string);
        returned_key char(18);
end-pr;

dcl-pr readDataQueue int(10) extproc('readDataQueue');
        dtaq_name char(10) const;
        dtaq_lib  char(10) const;
        key  pointer value options(*string);
        output varchar(1000);
end-pr;

dcl-s dtaq_in  char(10) inz('D16       ');
dcl-s dtaq_out char(10) inz('D18KEYED  ');
dcl-s dtaq_lib char(10) inz('JONATHAN  ');
dcl-s data_length int(10) inz(28);
dcl-s num_messages int(10) inz(1000);
dcl-s keys char(18) dim(1000);
dcl-s i int(10);
dcl-s message char(256);
dcl-s res char(256);
dcl-s returned_key char(18);
dcl-s output varchar(1000);

dcl-s flowers char(256) dim(28);
flowers(1) = '[7.47,2.47,5.34,2.34]';
flowers(2) = '[5.12,2.28,6.81,0.39]';
flowers(3) = '[7.16,3.70,4.58,1.24]';
flowers(4) = '[4.99,2.92,4.22,2.23]';
flowers(5) = '[6.09,4.31,2.46,1.96]';
flowers(6) = '[6.22,4.29,6.53,1.46]';
flowers(7) = '[5.54,2.13,2.74,0.43]';
flowers(8) = '[7.46,3.85,4.70,1.51]';
flowers(9) = '[7.76,3.43,6.54,0.64]';
flowers(10) = '[7.68,3.65,6.63,2.35]';
flowers(11) = '[7.03,2.31,6.68,0.78]';
flowers(12) = '[6.44,2.68,5.40,2.13]';
flowers(13) = '[5.97,2.56,6.14,0.56]';
flowers(14) = '[4.56,2.60,3.33,0.79]';
flowers(15) = '[4.94,2.94,6.77,0.68]';
flowers(16) = '[7.85,2.82,4.00,1.28]';
flowers(17) = '[4.66,4.10,2.66,2.31]';
flowers(18) = '[7.43,2.85,3.06,0.56]';
flowers(19) = '[5.29,2.62,2.28,0.32]';
flowers(20) = '[4.70,2.54,1.29,2.42]';
flowers(21) = '[5.92,4.22,3.32,0.81]';
flowers(22) = '[6.64,3.18,2.29,1.13]';
flowers(23) = '[5.25,2.10,3.07,0.47]';
flowers(24) = '[6.00,2.44,1.75,2.43]';
flowers(25) = '[6.10,2.44,5.30,0.19]';
flowers(26) = '[7.13,4.25,5.67,0.74]';
flowers(27) = '[6.80,2.37,5.09,0.37]';
flowers(28) = '[7.76,2.48,2.66,0.63]';

for i = 1 to num_messages;
    message = flowers(%REM(i:data_length) + 1);
    writeDataQueue(dtaq_in : dtaq_lib : %TRIM(message): returned_key);
    snd-msg ('Writing message ' + %TRIM(message));
    keys(i) = returned_key;
endfor;

for i = 1 to num_messages;
    readDataQueue(dtaq_out : dtaq_lib : keys(i): output);
    snd-msg ('Received message key ' + keys(i) + ', value ' + output);
endfor;

*inlr = *on;
return;
