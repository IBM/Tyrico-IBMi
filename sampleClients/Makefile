BUILDLIB:=TYRICO

clean:
	-system "DLTPGM PGM(${BUILDLIB}/SAMPLECPP)"
	-system "DLTPGM PGM(${BUILDLIB}/SAMPLERPG)"
	-system "DLTMOD MODULE(${BUILDLIB}/SAMPLECPP)"
	-system "DLTMOD MODULE(${BUILDLIB}/SAMPLERPG)"

create_sample_cpp_mod:
	system "CRTCPPMOD MODULE(${BUILDLIB}/SAMPLECPP) SRCSTMF('$(CURDIR)/sampleCustomerProgram.cpp') OPTION(*EVENTF) SYSIFCOPT(*IFS64IO) DBGVIEW(*SOURCE) TERASPACE(*YES *TSIFC) STGMDL(*SNGLVL) DTAMDL(*p128) DEFINE(DEBUG_ENABLED) OUTPUT(*PRINT) TGTCCSID(37)" 

create_sample_cpp_pgm: create_sample_cpp_mod
	system "CRTPGM PGM(${BUILDLIB}/SAMPLECPP) MODULE(${BUILDLIB}/SAMPLECPP ${BUILDLIB}/TYRICO) ACTGRP(*CALLER)"

create_sample_rpg_mod:
	system "CRTRPGMOD MODULE(${BUILDLIB}/SAMPLERPG) srcstmf('$(CURDIR)/sampleCustomerProgram.rpgle') AUT() DBGVIEW(*ALL) OPTIMIZE() OPTION(*EVENTF) OUTPUT(*PRINT) TEXT('') TGTCCSID(*JOB) TGTRLS() INCDIR(*NONE) DEFINE()""

create_sample_rpg_pgm: create_sample_rpg_mod
	system "CRTPGM (${BUILDLIB}/SAMPLERPG)"

all: clean create_sample_rpg_pgm create_sample_cpp_pgm