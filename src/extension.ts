import * as vscode from 'vscode';

export function activate(context: vscode.ExtensionContext) {
  console.log('UCTO extension activated');

  const initCommand = vscode.commands.registerCommand('ucto.init', () => {
    vscode.window.showInformationMessage('UCTO: Init Project triggered');
    // TODO: Scaffold project folders (frontend, backend, docs)
  });

  const sprintCommand = vscode.commands.registerCommand('ucto.sprint', () => {
    vscode.window.showInformationMessage('UCTO: Run Sprint triggered');
    // TODO: Generate scaffolds + BRD/UCD/TCD
  });

  const agentCommand = vscode.commands.registerCommand('ucto.agent', () => {
    vscode.window.showInformationMessage('UCTO: Trigger Agent triggered');
    // TODO: Trigger BA/Developer/Tester/Compliance/UX/Architect agents
  });

  const deployCommand = vscode.commands.registerCommand('ucto.deploy', () => {
    vscode.window.showInformationMessage('UCTO: Deploy triggered');
    // TODO: Containerize and push to VPS/Kubernetes
  });

  context.subscriptions.push(initCommand, sprintCommand, agentCommand, deployCommand);
}

export function deactivate() {
  console.log('UCTO extension deactivated');
}
