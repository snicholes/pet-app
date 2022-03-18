checkLogin().then(() => {
    console.log(loggedInPerson);
    if (loggedInPerson.pets || loggedInPerson.pets.size > 0)
        showPets(loggedInPerson.pets)
    else {
        document.getElementById('adoptedPets').remove();

        let noPetsMsg = document.createElement('p');
        noPetsMsg.innerText = 'Hmm... you don\'t have any pets yet! Try adopting some on the available pets page!';
        document.getElementsByTagName('main')[0].appendChild(noPetsMsg);
    }
});

function showPets(pets) {
    let petsTable = document.getElementById('adoptedPets');

    for (let pet of pets) {
        let rowForPet = document.createElement('tr');

        for (let field in pet) {
            let column = document.createElement('td');
            if (field!=='status') {
                column.innerText = pet[field];
            }
            rowForPet.appendChild(column);
        }
        petsTable.appendChild(rowForPet);
    }
}