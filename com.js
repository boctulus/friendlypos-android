#!/usr/bin/env node
/**
 * Wrapper para ejecutar los comandos del CLI desde la raíz del proyecto
 * Permite usar: node com [grupo] [comando] [args]
 */

import { runCLI } from './cmd/core/com.js';

// Ejecutar el CLI
runCLI().catch(error => {
  console.error('❌ Error fatal en el CLI:', error.message);
  process.exit(1);
});
