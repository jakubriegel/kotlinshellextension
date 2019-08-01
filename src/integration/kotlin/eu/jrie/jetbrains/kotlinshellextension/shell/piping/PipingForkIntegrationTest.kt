package eu.jrie.jetbrains.kotlinshellextension.shell.piping

class PipingForkIntegrationTest : PipingBaseIntegrationTest() {

    //    @Test
//    fun `should fork stderr for single process`() {
//        // given
//        val n = 5
//        val code = scriptFile(n)
//
//        // when
//        shell {
//            val script = systemProcess {
//                cmd {
//                    cmd = "./${code.name}"
//                }
//            }
//
//            (script forkErr { it pipe storeResult }) pipe nulloutOld await all
//        }
//
//        // then
//        assertEquals(scriptStdErr(n), readResult())
//    }
//
//    @Test
//    fun `should pipe forked stderr`() {
//        // given
//        val n = 5
//        val code = scriptFile(n)
//        val pattern = "2"
//
//        // when
//        shell {
//            val script = systemProcess {
//                cmd {
//                    cmd = "./${code.name}"
//                }
//            }
//
//            val grep = systemProcess {
//                cmd {
//                    "grep" withArg pattern
//                }
//            }
//
//            (script forkErr { it pipe grep pipe storeResult }) pipe nulloutOld await all
//        }
//
//        // then
//        assertEquals(scriptStdErr(n).grep(pattern), readResult())
//    }
//
//    @Test
//    fun `should fork stderr in pipeline`() {
//        // given
//        val n = 5
//        val code = scriptFile(n)
//
//        // when
//        shell {
//            val ls = systemProcess { cmd = "ls" }
//
//            val script = systemProcess {
//                cmd {
//                    cmd = "./${code.name}"
//                }
//            }
//
//            ls pipe (script forkErr { it pipe storeResult }) pipe nulloutOld await all
//        }
//
//        // then
//        assertEquals(scriptStdErr(n), readResult())
//    }

//    @Test
//    fun `should pipe stdout to process correctly after forking`() {
//        // given
//        val n = 5
//        val code = scriptFile(n)
//        val pattern = "2"
//
//        // when
//        shell {
//            val grep = systemProcess {
//                cmd {
//                    "grep" withArg pattern
//                }
//            }
//
//            val script = systemProcess {
//                cmd {
//                    cmd = "./${code.name}"
//                }
//            }
//
//            (script forkErr nulloutOld) pipe grep pipe storeResult await all
//        }
//
//        // then
//        assertEquals(scriptStdOut(n).grep(pattern), readResult())
//    }

//    @Test
//    fun `should pipe stdout to file correctly after forking`() {
//        // given
//        val n = 5
//        val code = scriptFile(n)
//        val file = file()
//
//        // when
//        shell {
//            val script = systemProcess {
//                cmd {
//                    cmd = "./${code.name}"
//                }
//            }
//
//            (script forkErr nulloutOld) pipe file await all
//        }
//
//        // then
//        assertEquals(scriptStdOut(n), file.readText())
//    }
//

    ////    @Test
////    fun `should pipe stdout to null`() {
////        // given
////        val outFile = file("console")
////        System.setOut(PrintStream(outFile))
////
////        val n = 5
////        val code = scriptFile(n)
////
////        // when
////        shell {
////            val script = systemProcess {
////                cmd {
////                    cmd = "./${code.name}"
////                }
////            }
////
////            (script forkErr { it pipe storeResult }) pipe nulloutOld await all
////        }
////
////        // then
////        assertEquals("", outFile.withoutLogs())
////    }
////
////    @Test
////    fun `should pipe stderr to null`() {
////        // given
////        val outFile = file("console")
////        System.setOut(PrintStream(outFile))
////
////        val n = 5
////        val code = scriptFile(n)
////
////        // when
////        shell {
////            val script = systemProcess {
////                cmd {
////                    cmd = "./${code.name}"
////                }
////            }
////
////            (script forkErr nulloutOld) pipe storeResult await all
////        }
////
////        // then
////        assertEquals("", outFile.withoutLogs())
////    }
////
////    @Test
////    fun `should pipe stdout and stderr to null`() {
////        // given
////        val outFile = file("console")
////        System.setOut(PrintStream(outFile))
////
////        val n = 5
////        val code = scriptFile(n)
////
////        // when
////        shell {
////            val script = systemProcess {
////                cmd {
////                    cmd = "./${code.name}"
////                }
////            }
////
////            (script forkErr nulloutOld) pipe nulloutOld await all
////        }
////
////        // then
////        assertEquals("", outFile.withoutLogs())
////    }

}