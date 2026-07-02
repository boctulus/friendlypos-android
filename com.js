#!/usr/bin/env node
/**
 * Wrapper para ejecutar los comandos del CLI desde la raíz del proyecto
 * Permite usar: node com [grupo] [comando] [args]
 */

import { runCLI } from './cmd/core/com.js';

// Shorthand: "node com.js pack [flags]" -> "node com pack pack [flags]"
if (process.argv[2] === 'pack') {
  const next = process.argv[3];
  if (next === undefined || next.startsWith('-')) {
    process.argv.splice(3, 0, 'pack');
  }
}

// Ejecutar el CLI
runCLI().catch(error => {
  console.error('❌ Error fatal en el CLI:', error.message);
  process.exit(1);
});
