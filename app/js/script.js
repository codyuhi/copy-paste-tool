let s;
let b;

function editButton() {
  let allSections = JSON.parse(localStorage.allSections);
  let activeButton = JSON.parse(localStorage.activeButton);
  let newButtonName = document.getElementById("buttonNameInput").value;
  let newButtonDescription = JSON.stringify(
    document.getElementById("buttonPasteValue").value
  );
  s = activeButton.section;
  b = activeButton.button;
  promptEditButton();
  document.querySelector("#editNameInput").value =
    allSections[activeButton.section[s]].sectionButtons[b].buttonName;
  document.querySelector("#editPasteValue").value =
    allSections[activeButton.section[s]].sectionButtons[b].pasteValue;
  document
    .querySelector("#editSubmitButton")
    .setAttribute("onclick", "saveButton()");
}

function handleBranding() {
  let version = document.getElementById("version");
  let toolBranding = document.getElementById("toolbranding");
  let companyBrandingPosition = document.getElementById("companybranding");

  if (window.innerWidth < 1215) {
    version.hidden = true;
    toolBranding.hidden = true;
    companyBrandingPosition.style.position = "absolute";
    companyBrandingPosition.innerText = "company";
  } else {
    version.hidden = false;
    toolBranding.hidden = false;
    companyBrandingPosition.style.position = "relative";
    companyBrandingPosition.innerText = "company";
  }
}

function closeDialog() {
  let dialogs = document.querySelectorAll("dialog");
  for (i = 0; i < dialogs.length; i++) {
    dialogs[i].style.display = "none";
  }
}

function promptNewSection() {
  closeDialog();
  let dialog = document.querySelector("#createsectiondialog");
  dialog.style.display = "block";
}

function showSectionReorder() {
  closeDialog();
  let dialog = document.querySelector("#reorderSectionsDialog");
  dialog.style.display = "block";

  let s = document.querySelectorAll(".sectionElement");
  for (var j = 0; j < s.length; j++) {
    s[j].remove();
  }

  if (!localStorage.allSections || localStorage.allSections != "[]") {
    let allSections = JSON.parse(localStorage.allSections);
    allSections.forEach((e) => {
      let node = document.createElement("div");
      node.className = "sectionElement";
      node.innerHTML = `<p class="sectionElement" name="section" value="${e.sectionName}"><span class="sectionTitle"> ${e.sectionName} </span>
                        <input class="reorderTextInput" type=text/></p>`;
      document.querySelector("#reorderSections").appendChild(node);
    });
  } else {
    alert("There are no sections to reorder!");
    closeDialog();
  }
}

function confirmSectionReorder() {
  let numbers = document.querySelectorAll(".reorderTextInput");
  let newOrder = [];
  for (let index = 0; index < numbers.length; index++) {
    const element = numbers[index];
  }
  let newSections = [];
  let allSections = JSON.parse(localStorage.allSections);
  let j = 0;
  // localStorage.clear();
  for (i = 0; i < newOrder.length; i++) {
    // try {
    if (newOrder[i] > newOrder.length) {
      alert(
        `Your input for section "${allSections[i].sectionName}" is invalid`
      );
    } else {
      console.log(JSON.stringify(allSections[numbers[1]]));
      //  newSections[j] = JSON.stringify(allSections[numbers[i].value - 1]);
      // numbers.forEach(element => {
      //     console.log(element)
      // let newSection = `{"sectionName": "` + JSON.parse(numbers[i]).sectionName + `","sectionButtons": ` +
      //     JSON.stringify(JSON.parse(numbers[i]).sectionButtons) + `}`;
      // allSections.push(newSection);
      // });
      // localStorage.allSections = JSON.stringify(allSections);
    }
    // } catch {
    //     alert("Please ensure that you've inputted valid numbers and that there are no duplicates.")
    // }
    j++;
  }
  // console.log(localStorage.allSections)
  // console.log(newSections)
  // localStorage.clear();
  // localStorage.allSections = newSections;
  // loadSections();
}

function createSection() {
  let allSections = JSON.parse(localStorage.allSections);
  let newSectionName = document.getElementById("sectionNameInput").value;
  let newSection = JSON.parse(
    `{"sectionName": "` + newSectionName + `","sectionButtons": []}`
  );
  allSections.push(newSection);
  localStorage.allSections = JSON.stringify(allSections);
  let midempty = document.getElementById("midempty");
  midempty.style.visibility = "hidden";
  loadSections();
  loadSideNav();
  document.getElementById("sectionNameInput").value = "";
  closeDialog();
}

function deleteSection(id) {
  let allSections = JSON.parse(localStorage.allSections);
  let favoriteButtons = JSON.parse(localStorage.favoriteButtons);
  if (
    confirm(
      'Are you sure you want to delete the section "' +
        allSections[id].sectionName +
        '" and all of its buttons?'
    )
  ) {
    delete allSections[id];
    allSections.splice(id, 1);
    localStorage.allSections = JSON.stringify(allSections);
    let originalLength = favoriteButtons.length;
    for (var i = 0; i < originalLength; i++) {
      if (favoriteButtons[i].sectionId === id) {
        delete favoriteButtons[i];
        favoriteButtons.splice(i, 1);
        localStorage.favoriteButtons = JSON.stringify(favoriteButtons);
      }
    }
    loadFavNav();
    loadSections();
    loadSideNav();
  }
}

function loadSections() {
  let allSections = JSON.parse(localStorage.allSections);
  let midcontainer = document.getElementById("midcontainer");

  if (!localStorage.allSections || localStorage.allSections === "[]") {
    let midempty = document.getElementById("midempty");
    midempty.style.visibility = "visible";
  }

  if (allSections.length >= 5) {
    document.getElementById("midnav").style.overflowY = "visible";
  } else {
    document.getElementById("midnav").style.webkit = "hidden";
  }

  while (midcontainer.firstChild) {
    midcontainer.removeChild(midcontainer.firstChild);
  }

  for (var i = 0; i < allSections.length; i++) {
    let sectionDiv = document.createElement("div");
    let sectionName = document.createElement("h3");
    let deleteSection = document.createElement("button");
    let addButton = document.createElement("button");
    let hr = document.createElement("hr");
    let titleDiv = document.createElement("div");
    let titleButtonsDiv = document.createElement("div");

    sectionDiv.id = "section" + i;
    sectionName.innerText = allSections[i].sectionName;
    deleteSection.innerText = "Delete Section";
    deleteSection.className = "deleteSectionButton";
    deleteSection.id = "section" + i + "del";
    addButton.innerText = "Add Button";
    addButton.className = "addButtonButton";
    addButton.id = "abs" + i; // Stands for "add button for section"

    deleteSection.setAttribute("onclick", 'deleteSection("' + i + '")');
    addButton.setAttribute("onclick", "promptNewButton(" + i + ")");

    titleDiv.appendChild(sectionName);
    titleButtonsDiv.appendChild(deleteSection);
    titleButtonsDiv.appendChild(addButton);

    midcontainer.appendChild(sectionDiv);
    sectionDiv.appendChild(titleDiv);
    sectionDiv.appendChild(titleButtonsDiv);
    sectionDiv.appendChild(hr);

    if (allSections[i].sectionButtons.length === 0) {
      let h4 = document.createElement("h4");
      h4.innerText = "No Buttons Created for this Section";
      sectionDiv.appendChild(h4);
    }

    for (var j = 0; j < allSections[i].sectionButtons.length; j++) {
      let button = document.createElement("button");
      button.innerText = allSections[i].sectionButtons[j].buttonName;
      button.id = "sec" + i + "but" + j;
      button.setAttribute(
        "onclick",
        "copyPasteButtonPressed(" + i + ", " + j + ")"
      );
      button.setAttribute(
        "oncontextmenu",
        "copyPasteButtonRightClicked(" + i + "," + j + ")"
      );
      button.title = allSections[i].sectionButtons[j].pasteValue;
      sectionDiv.appendChild(button);
    }

    sectionDiv.appendChild(document.createElement("hr"));
  }
}

function copyPasteButtonRightClicked(i, j) {
  event.preventDefault();
  let mouseX = event.clientX;
  let mouseY = event.clientY;
  let favDel = document.getElementById("favdel");
  favDel.style.left = mouseX + "px";
  favDel.style.top = mouseY + "px";
  favDel.style.visibility = "visible";
  favDel.style.display = "block";
  localStorage.activeButton =
    '{ "section": "' + i + '", "button": "' + j + '"}';
  window.addEventListener("click", function () {
    favDel.style.visibility = "hidden";
    localStorage.removeItem("activeButton");
  });
}

function promptNewButton(id) {
  closeDialog();
  localStorage.activeSection = id;
  let dialog = document.querySelector("#createbuttondialog");
  dialog.style.display = "block";
}

function promptEditButton() {
  closeDialog();
  let dialog = document.querySelector("#editbuttondialog");
  dialog.style.display = "block";
}

function createButton() {
  let allSections = JSON.parse(localStorage.allSections);
  let newButtonName = document.getElementById("buttonNameInput").value;
  let newButtonDescription = JSON.stringify(
    document.getElementById("buttonPasteValue").value
  );
  let newButton = JSON.parse(
    `{"buttonName": "` +
      newButtonName +
      `", "pasteValue": ` +
      newButtonDescription +
      `}`
  );
  allSections[parseInt(localStorage.activeSection)].sectionButtons.push(
    newButton
  );
  localStorage.allSections = JSON.stringify(allSections);
  loadSections();
  document.getElementById("buttonNameInput").value = "";
  document.getElementById("buttonPasteValue").value = "";
  localStorage.removeItem("activeSection");
  closeDialog();
}

function saveButton() {
  let allSections = JSON.parse(localStorage.allSections);
  allSections[s].sectionButtons[b].buttonName =
    document.querySelector("#editNameInput").value;
  allSections[s].sectionButtons[b].pasteValue =
    document.querySelector("#editPasteValue").value;
  localStorage.allSections = JSON.stringify(allSections);
  loadSections();
  loadFavNav();
  loadSideNav();
  closeDialog();
  s = -1;
  b = -1;
}

function copyPasteButtonPressed(sectionId, buttonId) {
  closeDialog();
  let allSections = JSON.parse(localStorage.allSections);
  if (event.which === 1) {
    let pasteValue = allSections[sectionId].sectionButtons[buttonId].pasteValue;
    let el = document.createElement("textarea");
    el.value = pasteValue;
    document.body.appendChild(el);
    el.select();
    document.execCommand("copy");
    document.body.removeChild(el);
    let x = document.getElementById("snackbar");
    x.innerText =
      'Copied "' +
      allSections[sectionId].sectionButtons[buttonId].buttonName +
      "\"'s message to the clipboard!";
    x.className = "show";
    setTimeout(function () {
      x.className = x.className.replace("show", "");
    }, 3000);
  }
}

function addFavorite() {
  let allSections = JSON.parse(localStorage.allSections);
  let activeButton = JSON.parse(localStorage.activeButton);
  let favoriteButtons = JSON.parse(localStorage.favoriteButtons);
  var newButton =
    allSections[activeButton.section].sectionButtons[activeButton.button];
  newButton.sectionId = activeButton.section;
  newButton.buttonId = activeButton.button;
  favoriteButtons.push(newButton);
  localStorage.favoriteButtons = JSON.stringify(favoriteButtons);
  loadFavNav();
}

function loadSideNav() {
  let allSections = JSON.parse(localStorage.allSections);
  let sideNavCells = document.getElementById("sidenavcells");

  while (sideNavCells.firstChild) {
    sideNavCells.removeChild(sideNavCells.firstChild);
  }

  for (var i = 0; i < allSections.length; i++) {
    let a = document.createElement("a");
    a.href = "#section" + i;
    let innerDiv = document.createElement("div");
    innerDiv.innerText = allSections[i].sectionName;
    a.appendChild(innerDiv);
    sideNavCells.appendChild(a);
    sideNavCells.appendChild(document.createElement("hr"));
  }
}

function loadFavNav() {
  let favoriteButtons = JSON.parse(localStorage.favoriteButtons);
  let favNavCells = document.getElementById("favnavcells");

  if (favoriteButtons.length >= 1) {
    favnavempty.style.visibility = "hidden";
  } else {
    favnavempty.style.visibility = "visible";
  }

  while (favNavCells.firstChild) {
    favNavCells.removeChild(favNavCells.firstChild);
  }

  for (var i = 0; i < favoriteButtons.length; i++) {
    let favNavCell = document.createElement("p");
    favNavCell.innerText = favoriteButtons[i].buttonName;
    favNavCell.title = favoriteButtons[i].pasteValue;
    favNavCell.id = "#fav" + i;
    favNavCell.setAttribute("onclick", "favCellClicked(" + i + ")");
    favNavCell.setAttribute("oncontextmenu", "favRightClicked(" + i + ")");
    favNavCells.appendChild(favNavCell);
    favNavCells.appendChild(document.createElement("hr"));
  }
}

function favCellClicked(id) {
  closeDialog();
  let favoriteButtons = JSON.parse(localStorage.favoriteButtons);
  if (event.which === 1) {
    let pasteValue = favoriteButtons[id].pasteValue;
    let el = document.createElement("textarea");
    el.value = pasteValue;
    document.body.appendChild(el);
    el.select();
    document.execCommand("copy");
    document.body.removeChild(el);
    let x = document.getElementById("snackbar");
    x.innerText =
      'Copied "' +
      favoriteButtons[id].buttonName +
      "\"'s message to the clipboard!";
    x.className = "show";
    setTimeout(function () {
      x.className = x.className.replace("show", "");
    }, 3000);
  }
}

function favRightClicked(id) {
  event.preventDefault();
  let mouseX = window.innerWidth - event.clientX;
  let mouseY = event.clientY - 40;
  let favDel = document.getElementById("favnavdropdown");
  favDel.style.top = mouseY + "px";
  favDel.style.visibility = "visible";
  favDel.style.display = "block";
  localStorage.activeFav = '{ "button": "' + id + '"}';
  window.addEventListener("click", function () {
    favDel.style.visibility = "hidden";
    localStorage.removeItem("activeFav");
  });
}

window.onresize = function (event) {
  this.handleBranding();
};

function checkEnter() {
  if (event.key == "Enter") {
    document.getElementById("sectionSubmit").click();
  }
}

window.onload = function () {
  this.handleBranding();
  if (localStorage.allSections && localStorage.allSections != "[]") {
    this.loadSections();
    this.loadSideNav();
  } else {
    localStorage.allSections = "[]";
    let midempty = document.getElementById("midempty");
    midempty.style.visibility = "visible";
    let favnavempty = document.getElementById("favnavempty");
    favnavempty.style.visibility = "visible";
  }

  if (localStorage.favoriteButtons && localStorage.favoriteButtons != "[]") {
    this.loadFavNav();
  } else {
    localStorage.favoriteButtons = "[]";
    let favnavempty = document.getElementById("favnavempty");
    favnavempty.style.visibility = "visible";
  }
};

function settingsMenu() {
  closeDialog();
  let dialog = document.querySelector("#settingsdialog");
  dialog.style.display = "block";
}

function clearAll() {
  closeDialog();

  if (
    confirm(
      "THIS WILL DELETE ALL OF YOUR SECTIONS AND THEIR BUTTONS!\nAre you sure you want to delete all sections and all of their buttons?"
    )
  ) {
    localStorage.clear();
    localStorage.allSections = "[]";
    localStorage.favoriteButtons = "[]";
    loadFavNav();
    loadSections();
    loadSideNav();
  }
}

function importItems() {
  closeDialog();
  let dialog = document.querySelector("#importdialog");
  dialog.style.display = "block";
}

function exportItems() {
  closeDialog();
  let c = document.querySelectorAll(".checkbox");
  for (var j = 0; j < c.length; j++) {
    c[j].remove();
  }
  let dialog = document.querySelector("#exportdialog");
  dialog.style.display = "block";
  if (!localStorage.allSections || localStorage.allSections != "[]") {
    let node = document.createElement("div");
    node.className = "checkbox";
    node.innerHTML = `<input type="checkbox" class="checkbox" id="allsections" name="allsections" value="allsections"> Select all </input><br><br>`;
    document.querySelector("#exportcheckboxes").appendChild(node);
    let allSections = JSON.parse(localStorage.allSections);
    allSections.forEach((e) => {
      let node = document.createElement("div");
      node.className = "checkbox";
      node.innerHTML = `<input type="checkbox" class="checkbox" name="section" value="${e.sectionName}"> ${e.sectionName} </input><br><br>`;
      document.querySelector("#exportcheckboxes").appendChild(node);
    });
  } else {
    alert("There are no sections to export!");
    closeDialog();
  }
  var checkbox = document.querySelector("input[id=allsections]");

  checkbox.addEventListener("change", function () {
    if (this.checked) {
      let b = document.querySelectorAll("input[name=section]");
      for (var j = 0; j < b.length; j++) {
        b[j].checked = true;
      }
    } else {
      let b = document.querySelectorAll("input[name=section]");
      for (var j = 0; j < b.length; j++) {
        b[j].checked = false;
      }
    }
  });
}

function confirmExport() {
  let boxes = document.querySelectorAll("input[name=section]:checked");
  let exportedsections = [];
  let exportjson = [];
  if (document.querySelector("#allsections").checked == true) {
    document.querySelectorAll("input[name=section]").checked = false;

    let sections = JSON.parse(localStorage.allSections);
    sections.forEach((e) => {
      exportjson.push(JSON.stringify(e));
    });
    saveText(JSON.stringify(exportjson), "chat sections export.json");
    closeDialog();
    return;
  }
  boxes.forEach((e) => {
    exportedsections.push(e.value);
  });
  let sections = JSON.parse(localStorage.allSections);
  sections.forEach((e) => {
    exportedsections.forEach((f) => {
      if (e.sectionName === f) {
        exportjson.push(JSON.stringify(e));
      }
    });
  });
  saveText(JSON.stringify(exportjson), "chat sections export.json");
  closeDialog();
}

function saveText(text, filename) {
  var a = document.createElement("a");
  a.setAttribute(
    "href",
    "data:text/plain;charset=utf-u," + encodeURIComponent(text)
  );
  a.setAttribute("download", filename);
  a.click();
}

function deleteButton() {
  let allSections = JSON.parse(localStorage.allSections);
  let activeButton = JSON.parse(localStorage.activeButton);
  let favoriteButtons = JSON.parse(localStorage.favoriteButtons);

  delete allSections[activeButton.section].sectionButtons[activeButton.button];
  allSections[activeButton.section].sectionButtons.splice(
    activeButton.button,
    1
  );
  localStorage.allSections = JSON.stringify(allSections);
  localStorage.removeItem("activeButton");
  loadFavNav();
  loadSections();
}

function deleteFavorite() {
  let favoriteButtons = JSON.parse(localStorage.favoriteButtons);
  let activeFav = JSON.parse(localStorage.activeFav);
  delete favoriteButtons[activeFav.button];
  favoriteButtons.splice(activeFav.button, 1);
  localStorage.favoriteButtons = JSON.stringify(favoriteButtons);
  localStorage.removeItem("activeFav");
  loadFavNav();
}
